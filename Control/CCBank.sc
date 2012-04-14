
/*

given a list of names, 
it makes a gui
click learn and set each one,
post or save

its reloadable and reditable

usage:

ccresponder = ccbank.xfader
ccresponder.function = {  arg src, chan, num, val;  };

or

ccresponder = ccbank.responder('xfader',{ arg val;   });


CCBank([
	\xfader,
	\level,
	\one,
	\two,
	\three,
	\four,
	\pressure1,
	\pressure2,
	\pressure3
	\pressure4,
	\fx3,
	\fx4,
	\fxPressure3,
	\fxPressure4
]).gui

*/



CCBank {

	var <>sets,responders,tbs;
	
	*new { arg sets;
		^super.newCopyArgs(sets ? []).init
	}
	storeArgs {
		^[sets]
	}
	init {
		sets = sets.collect({ arg na; 
								if(na.isKindOf(Association).not,{ na -> nil },{ na 	})
							});
		responders = IdentityDictionary.new;
	}
	*responderClass { ^CCResponder }	
	guiBody { arg layout;
		SaveConsole(this,nil,layout).print.save;
		tbs = Array.newClear(sets.size);
		sets.do { arg assc,i;
			var n,c;
			layout.startRow;
			this.guiOne(layout,assc,i);
		};
	}
	guiOne { arg layout,assc,i;
		var tb,oneShot,ne;
		tb = ToggleButton(layout,"Learn",{ arg tb,state;
				tbs.do { arg t; if(t !== tb,{ t.toggle(false) }) };
				oneShot = this.class.responderClass.new({ |port,chan,num,value|
								{ ne.activeValue = num; 	}.defer
							},nil,nil,nil,nil,true,true);
				layout.removeOnClose(oneShot);
			},{
				oneShot.remove;
				oneShot = nil;
			});
		tbs[i] = tb;
		CXLabel(layout,assc.key.asString,minWidth:100);
		ne = NumberEditor(assc.value ? 128,ControlSpec(1,128,\lin,1));
		ne.action = { arg cnum;
			var ccr;
			sets[i].value = if(cnum == 128,{nil},{cnum.asInteger});
			// set any currently active one
			ccr = responders[assc.key];
			if(ccr.notNil,{
				ccr.matchEvent = MIDIEvent(nil,ccr.matchEvent.port,ccr.matchEvent.chan,sets[i].value,nil);
			})
		};
		ne.smallGui(layout);
		//ve = CXLabel(layout,"",minWidth:50);
	}
	at { arg key;
		var ccr,assc;
		^responders[key] ?? {
			assc = this.findSet(key) ?? { (key.asString + "not found in" + this).warn };
			// would be better: if nil then do not install but allow it to learn and get installed later
			ccr = this.class.responderClass.new(nil, nil, nil, assc.value ? 127, nil);
			responders[key] = ccr;
			ccr
		}
	}
	responder { arg key, function;
		var ccr;
		ccr = this.at(key);
		ccr.function = { |src,chan,num,value| function.value(value) };
		^ccr
	}
	doesNotUnderstand { |key ... args|
		var	result,set;
		set = this.findSet(key);
		if(set.isNil,{
			(key.asString + "not found in " + this).error;
			DoesNotUnderstandError(this, key, args).throw;
		},{
			^this.at(key)
		})
	}
		
	free {
		responders.keysValuesDo { arg k,ccr;
			ccr.remove
		};
		responders = IdentityDictionary.new;
	}
	keys {
		^sets.collect(_.key)
	}
	findSet { arg key;
		^sets.detect({ arg set; set.key == key })
	}
}


NoteOnBank : CCBank {
	
	*responderClass { ^NoteOnResponder }
	
}
