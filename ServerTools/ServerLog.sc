

ServerLog : NetAddr {

	var <msgs,<>tail=false,<server;
	var lastStatus;

	*new { arg server;
		server = server ? Server.default;
		if(server.addr.isKindOf(ServerLog),{ ^server.addr });
		^super.new(server.addr.hostname,server.addr.port).slinit(server);
	}
	*forServer { arg server;
		server = server ? Server.default;
		if(server.addr.isKindOf(ServerLog),{ ^server.addr });
		^nil
	}		
	*start { |server|
		^this.new(server)
	}
	*stop { |server|
		this.forServer(server).remove
	}
	remove {
		server.addr = NetAddr(hostname,port);
	}

	*ifActive { arg server,func;
		var sl;
		sl = this.forServer(server);
		if(sl.notNil,{
			func.value(sl)
		})
	}

	
	filterBySynthDef { arg defName;
		^this.matchMsgs({ arg m; m.matchSynthDef(defName) })
	}
	filterBySynth { arg nodeID;
		^this.matchMsgs({arg m; m.matchSynth(nodeID) })
	}
	matchMsgs { arg matchFunc;
		var matches;
		matches = [];
		msgs.do { arg msg;
			var ms;
			ms = msg.matchMsgs(matchFunc);
			if(ms.notNil,{  matches = matches ++ ms })
		};
		^matches
	}
				
	*guiMsgsForSynth { arg synth,layout,showTimes=false;
		var msgs,sl;
		sl = this.forServer(synth.server) ?? { "ServerLog not running".inform; ^this };
		msgs = sl.filterBySynth(synth.nodeID);
		ServerLogGui(sl).showTimes_(showTimes).gui(layout,nil,msgs)
	}
	*guiMsgsForSynthDef { arg defName,layout,server;
		var msgs,sl;
		sl = this.forServer(server)  ?? { "ServerLog not running".inform; ^this };
		msgs = sl.filterBySynthDef(defName);
		ServerLogGui(sl).gui(layout,nil,msgs)
	}
		
	slinit { arg s;
		server = s;
		server.addr = this;
		thisProcess.recvOSCfunc = { arg time,replyAddr,msg;
			var status;
			if(msg[0] == '/status.reply') {
				status = msg[0..5];
				if(status != lastStatus,{
					msgs = msgs.add( ServerLogReceivedEvent(time,status) );
					lastStatus = status;
				});
			} {
				msgs = msgs.add( ServerLogReceivedEvent(time,msg) )
			}
		};
	}
	sendMsg { arg ... args;
		if(args != ["/status"],{
			msgs = msgs.add( ServerLogSentEvent( nil, args,false) );
			if(tail,{
			    args.postln;
			});
		});
		^super.sendMsg(*args);
	}
	sendBundle { arg time ... args;
		msgs = msgs.add( ServerLogSentEvent( time,args,true) );
		if(tail,{
		    args.postln;
		});
		^super.sendBundle(*([time]++args))
	}
	guiClass { ^ServerLogGui }

	getSortedEvents { arg tail,function;
		// list in logical time order

		/*
		// could roughly intersperse them
		// which is closer than what we start with
		a = Array.series(10,0,2);
		b = Array.series(15,1,2);
		if(b.size > a.size,{
			t = a;
			a = b;
			b = t;
		});
		c = Array(b.size + a.size);
		a.do({ |it,i|
			c.add(it);
			if(b[i].notNil,{ c.add(b[i]) })
		});

		c

		c.hoareFind(5,{|a,b| a < b })

		// sort just up to the end of what you will show
		c.hoareFind(10,{|a,b| a < b })

		c

		// better to do this from the back then

		*/

		Routine({
			var q,events,since,a,b;

			q = PriorityQueue.new;
			msgs.do({ |it| q.put(it.eventTime,it) });
			events = Array.fill(msgs.size,{ |i|
					if(i % 25 == 0,{0.05.wait});
					if(i % 250 == 0,{ 0.5.wait });
					q.pop
				});

			if(tail.notNil,{
				function.value( events.copyRange(events.size-tail-1,events.size-1) );
			},{
				function.value( events )
			})
		}).play(AppClock)
	}
	*cmdString { |cmd|
		if(cmd.asInteger != 0,{
			cmd = cmd.asInteger;
		});
		^cmd.switch(
			11 , { "/n_free" },
			12, {"/n_run"},
			14, {"/n_map"},
			48, {"/n_mapn"},
			15, {"/n_set"},
			16, {"/n_setn"},
			17, {"/n_fill"},
			10, {"/n_trace"},
			46, {"/n_query"},
			18, {"/n_before"},
			19, {"/n_after"},
			21, {"/g_new"},
			22, {"/g_head"},
			23, {"/g_tail"},
			24, {"/g_freeAll"},
			50, {"/g_deepFree"},
			9, {"/s_new"},
			44, {"/s_get"},
			45, {"/s_getn"},
			cmd.asString
		)
	}
	
	*gui { |parent,bounds,tail=1000,server|
		var sl,events;
		sl = this.new(server);
		sl.asyncGui(parent,bounds,tail);
	}
	asyncGui { arg parent,bounds,tail=1000;
		this.getSortedEvents(tail,{ |events|
			this.gui(parent,bounds,events)
		});
	}
	*report { |tail=1000|
		if(Server.default.addr.isKindOf(ServerLog),{
			Server.default.addr.report(tail)
		},{
			"ServerLog has not been running".inform;
		});
	}
	report { arg tail;
		this.events(tail).do({ |ev|
			ev.report
		})
	}	
}


ServerLogSentEvent {

	var <>delta,<>msg,<>isBundle,<>timeSent;

	*new { arg delta,msg,isBundle;
		^super.newCopyArgs(delta,msg,isBundle,Main.elapsedTime)
	}
	eventTime {
		^timeSent + (delta?0)
	}
	report {
		var msgFormat;
		// if(isBundle,{  TODO
			// i use the gui mostly

		(">>> % (% + %) % %".format(this.eventTime,timeSent,delta,this.cmdString,msg.copyToEnd(1))).postln
	}
	cmdString { ^ServerLog.cmdString(msg[0]) }
	matchMsgs { arg func;
		if(this.isBundle,{
			^msg.collect(ServerLogSentEvent(delta,_,false,timeSent)).select(func)
		},{
			if(func.value(this), { ^[this] }, { ^nil })
		})
	}
	// for non bundles only
	matchSynthDef { arg defName;
		var cmd;
		^(msg[1] == defName) and: {  
			cmd = ServerLog.cmdString(msg[0]);
			cmd == "/s_new" /* or: { cmd == "/d_recv" } */
			// have to decode the bytes
		}
	}
	matchSynth { arg nodeID;
		^(msg[2] == nodeID) and: {  ServerLog.cmdString(msg[0]) == "/s_new" }
	}
}


ServerLogReceivedEvent {

	var <>time,<>msg,<>timeReceived;

	*new { arg time,msg,isBundle;
		^super.newCopyArgs(time,msg,Main.elapsedTime)
	}
	eventTime {
		^time
	}
	report {
		var cmd, one, numUGens, numSynths, numGroups, numSynthDefs,
					avgCPU, peakCPU, sampleRate, actualSampleRate;
		if(msg[0] == '/status.reply',{
			#cmd, one, numUGens, numSynths, numGroups, numSynthDefs,
					avgCPU, peakCPU, sampleRate, actualSampleRate = msg;
			("<<< % % ugens % synths % groups % synthDefs".format(this.eventTime,numUGens,numSynths,numGroups,numSynthDefs)).postln
		},{
			("<<< % % %".format(this.eventTime,ServerLog.cmdString(msg[0]),msg.copyToEnd(1))).postln;
		});
	}
	isBundle {
		^false
	}
	matchMsgs { arg func;
		if(func.value(this), { ^[this] },{ ^nil })
	}
	matchSynthDef { ^false }
	matchSynth { arg nodeID;
		^(msg[1] == nodeID) and: {  ServerLog.cmdString(msg[0]) == "/n_go" }
	}
}

