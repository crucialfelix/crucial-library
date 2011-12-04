


AbstractConsole {

	var <>layout,<>defaultPath;

	getPathThen {  arg then ... args; }
	addToLayout { arg moreFunc;
		moreFunc.value(layout)
	}
}



SynthConsole : AbstractConsole  {

	var <>format, <>duration;
	var <>ugenFunc,<>onRecordOrWrite;
	var pauseControl;

	var tempoG;

	*new { arg ugenFunc,layout;
		^super.new.ugenFunc_(ugenFunc).layout_(layout.asPageLayout).format_(SoundFileFormats.new)
	}

	play {
		ActionButton(layout,">",{this.doPlay })
		    .background_(Color(0.0, 0.86567164179104, 0.28425038984184));
	}
	prepare {
		ActionButton(layout,"pre",{this.doPrepare})
		    .background_(Color(0.41676819981724, 0.92857142857143, 0.2771855010661, 0.2089552238806))
	}
	scope {arg duration=0.5;
		ActionButton(layout,"scope",{this.doScope(duration)})
	}
	fftScope {
		if(SCFreqScope.notNil,{
			ActionButton(layout,"FreqScope",{this.doFreqScope})
				.background_(Color.green);
		})
	}
	record { arg defpath;
		/*if(defpath.notNil,{ defaultPath = defpath });
		ActionButton(layout,"|*|",{
			this.getPathThen(\doRecord);
		}).background_(Color.red);
		*/
	}
	write {arg dur,defpath;
		//		if(defpath.notNil,{ defaultPath = defpath });
		//		ActionButton(layout, "{}",{ this.getPathThen(\doWrite,dur.value ? duration ?? { 120 }) } ); // do a dialog
	}

	stop { arg stopFunc;
		ActionButton(layout,"stop",{
			this.doStop(stopFunc)
		}).background_(Color(0.7910447761194, 0.20998949813831, 0.16529293829361));
	}
	free {
		ActionButton(layout,"free",{
			ugenFunc.free;
		});
	}
	formats {
		format.gui(layout);
	}

	tempo {
		Tempo.default.gui(layout);
	}

	// pr
	doPlay {
		this.ugenFunc.play;
	}
	doPrepare {
		this.ugenFunc.prepareForPlay
	}

	doScope { arg duration=0.5;
	    this.ugenFunc.scope(duration)
	}
	doFreqScope {
		if(SCFreqScopeWindow.notNil,{
			SCFreqScopeWindow(400, 200, 0);
		})
	}

	doStop { arg stopFunc;
		stopFunc.value;
		ugenFunc.stop;
		NotificationCenter.notify(this,\didStop);
	}

	doRecord {	arg path;
//		var hformat,sformat;
//		# hformat, sformat = this.getFormats;
//
//		Synth.record({arg synth;
//			Tempo.setTempo;
//			this.ugenFunc.value(synth)
//		},duration,path,hformat,sformat);
//		onRecordOrWrite.value(path);
//		NotificationCenter.notify(this,\didRecordOrWrite);
	}

	doWrite { arg path, argduration;
//		var hformat,sformat;
//		# hformat, sformat = this.getFormats;
//
//		Synth.write({arg synth;
//			Tempo.setTempo;
//			this.ugenFunc.value(synth)
//		} ,argduration ? duration,path,hformat,sformat);
//		onRecordOrWrite.value(path);
//		NotificationCenter.notify(this,\didRecordOrWrite);
	}

}




SaveConsole : AbstractConsole {

	var <>object;
	var <>path; // defaultPath may be a function
	var <>onSaveAs; // arg path

	*new { arg object, path,layout;
		^super.new.object_(object).path_(path)
			.layout_(layout ?? {PageLayout.new.front})
	}

	print {
		ActionButton(layout,"postcs",{ object.asCompileString.postln });
	}
	printPath {
		ActionButton(layout,"post path",{ path.value.asCompileString.postln })
	}
	save { arg title="save",minWidth=100;
		ActionButton(layout,title,{
			if(path.value.isNil,{
		 		this.getPathThen(\doSaveAs);
		 	},{
		 		this.doSave
		 	})
	 	},minWidth).background_(
	 		if(path.value.isNil,{ // virgin
	 			GUI.skin.background
	 		},{
	 			Color.new255(255,242,89)
	 		})
	 	);
	}
	saveAs { arg onSaveF,default;
		defaultPath = default ? defaultPath;
		onSaveAs = onSaveF ? onSaveAs;
		ActionButton(layout,"save as",{
			this.getPathThen(\doSaveAs,default);
	 	});
	}
	open { arg onOpenF;
		ActionButton(layout,"open",{
			GUI.dialog.getPaths({ arg paths;
				onOpenF.value(paths[0]);
			});
		});
	}
	getPathThen {  arg then ... args;
		GUI.dialog.savePanel({arg argpath;
			this.path = argpath;
			this.performList(then,[path] ++ args);
		});
	}

	doSave {
		var clobber,vpath,evpath;
		vpath = path.value;
		if(File.exists(vpath),{ // whoops, sorry microsoft
			evpath = vpath.escapeChar($ );
			("cp " ++ evpath + evpath ++ ".bak").unixCmd;
		});
		object.asCompileString.write(vpath);
	}
	doSaveAs {
		var clobber,vpath,evpath;
		vpath = path.value;
		if(File.exists(vpath),{
			evpath = vpath.escapeChar($ );
			("cp " ++ evpath + evpath ++ ".bak").unixCmd;
		});
		object.asCompileString.write(vpath);
		onSaveAs.value(vpath);
	}
}



SoundFileFormats { // an interface

	var format='float32';

	gui { arg layout;
		var items;
		items = #['float32', 'aiff16', 'aiff24','wav16'];
		GUI.popUpMenu.new(layout,Rect(0,0,80,16))
			.items_(items)
			.action_({ arg pop;
				format = items.at(pop.value);
			})
			.background_(Color.red(0.4,0.3));
	}

	headerFormat {
		if(format=='float32',{
			^'Sun'
		});
		if(format=='aiff16',{
			^'AIFF'
		});
		if(format=='aiff24',{
			^'AIFF'
		});
		if(format=='wav16',{
			^'WAV'
		});
		^nil
	}

	sampleFormat {
		if(format=='float32',{
			^'float32'
		});
		if(format=='aiff16',{
			^'int16'
		});
		if(format=='aiff24',{
			^'int24'
		});
		if(format=='wav16',{
			^'int16'
		});
		^nil
	}
}


