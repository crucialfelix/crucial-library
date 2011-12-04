

/*
	a utility class to prepare and precisely record a player to a soundfile
*/

PlayerRecorder {
		
	var <>player,recordBuf,responder;
	
	*new { arg player;
		^super.newCopyArgs(player)
	}
	
	record { arg path,endBeat,onComplete,recHeaderFormat='AIFF', recSampleFormat='int24',atTime,limit=true;

		var timeOfRequest,bus,group,server,do;
		if(player.isPlaying,{ ^Error("Cannot start record while playing").throw });
		timeOfRequest = Main.elapsedTime;
		server = Server.default;
		bus = Bus.audio(server,player.numChannels);
		if(path.isNil,{
			path = thisProcess.platform.recordingsDir +/+ player.asString.select(_.isFileSafe);
			if(thisProcess.platform.name == \windows) {
				path =  path ++ Main.elapsedTime.round(0.01)

			} {
				path = path ++ Date.localtime.asSortableString
			};
			path = path ++ "." ++ recHeaderFormat;
		});
		if(endBeat.isNil,{
			endBeat = player.beatDuration ? 64
		});
		do = {
			var bufLoaded=false,timeout=60;
			CmdPeriod.add(this);
			responder = OSCresponderNode(server.addr,'/b_info',{ arg time,responder,msg;
				if(msg[1] == recordBuf.bufnum,{
					bufLoaded = true;
					responder.remove;
				});
			});
			responder.add;
					
			recordBuf = Buffer.alloc(server, 65536, player.numChannels,{ arg buf;
				buf.writeMsg(path, recHeaderFormat, recSampleFormat, 0, 0, true,{|buf|["/b_query",buf.bufnum]});
			});
			{
				while { bufLoaded.not and: {timeout > 0} } {
					timeout = timeout - 0.1;
					0.1.wait
				};
				this.prRecord(path,endBeat,server.asGroup,bus,recordBuf,onComplete,recHeaderFormat,recSampleFormat, limit,atTime,timeOfRequest)
			}.fork
		};
		if(server.serverRunning.not,{
			server.startAliveThread(0.1,0.4);
			server.waitForBoot({
				if(server.dumpMode != 0,{
					server.stopAliveThread;
				});
				InstrSynthDef.clearCache(server);
				if(server.isLocal,{
					InstrSynthDef.loadCacheFromDir(server);
				});
				do.value;
				nil
			});
		},do);
	}

	prRecord { arg path, endBeat,parentGroup, bus, recordBuf,onComplete, recHeaderFormat='AIFF', recSampleFormat='int24',limit=true, atTime, timeOfRequest;
		var bundle,def,defName,group,synth;
		bundle = AbstractPlayer.bundleClass.new;
		group = Group.basicNew(parentGroup.server);
		bundle.add( group.addToTailMsg );
		player.prepareToBundle(group, bundle, false, bus);
		player.spawnToBundle(bundle);
		
		defName = "__player-record-" ++ player.numChannels;
		def = SynthDef(defName, { arg bufnum,busnum;
			var out;
			out = In.ar(busnum, player.numChannels);
			if(limit and: {recSampleFormat.asString.contains("int")},{
				out = Limiter.ar(out,-0.01.dbamp)
			});
			Out.ar(0, out ); // listen
			DiskOut.ar(bufnum, out);
		});
		bundle.addPrepare([ "/d_recv", def.asBytes]);

		synth = Synth.basicNew(	defName,player.server);
		bundle.add( synth.addToTailMsg(parentGroup,[\bufnum,recordBuf.bufnum,\busnum,bus.index]) );
		bundle.addFunction({
			var ender;
			ender = AbstractPlayer.bundleClass.new;
			ender.add(synth.freeMsg);
			ender.add(recordBuf.closeMsg(recordBuf.freeMsg));
			ender.addFunction({
				recordBuf = nil;
			});
			player.stopToBundle(ender);
			player.freeToBundle(ender);
			OSCSched.sched(endBeat,player.server,ender.messages,{
				ender.doFunctions;
				("Recording ended:" + path).inform;
				CmdPeriod.remove(this);
				onComplete.value(this,path)
			})
		});
		("Recording" + path + "beat duration:" + endBeat).inform;
		bundle.sendAtTime(player.server,atTime ? player.defaultAtTime,timeOfRequest);
	}
	cmdPeriod {
		if(recordBuf.notNil,{
			"Aborting recording and closing file.".inform;
		});
		this.free;
	}
	free {
		if(recordBuf.notNil,{
			recordBuf.close;
			recordBuf.free;
			recordBuf = nil;
		});
		responder.remove;
		CmdPeriod.remove(this);
	}
}

