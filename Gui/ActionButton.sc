// abstract
SCButtonAdapter : SCViewHolder {

	*initClass {
		Class.initClassTree(GUI);
	}
	makeView { arg layout,x,y;
		var rect;
		if((layout.isNil or: { layout.isKindOf(MultiPageLayout) }),{ layout = layout.asFlowView; });
		this.view = GUI.button.new(layout,Rect(0,0,x,y ? GUI.skin.buttonHeight));
		if(consumeKeyDowns,{ this.view.keyDownAction_({nil}) });
	}
	flowMakeView { arg layout,x,y;
		this.view = GUI.button.new(layout.asFlowView,Rect(0,0,x,y ? GUI.skin.buttonHeight));
		if(consumeKeyDowns,{ this.view.keyDownAction_({nil}); });
	}

	makeViewWithStringSize { arg layout,optimalWidth,minWidth,minHeight;
		this.makeView( layout,(optimalWidth + 10).max(minWidth?20),(minHeight ) )
	}
	initOneState { arg name,textcolor,backcolor;
		view.states_([[name,textcolor ? Color.black, backcolor ? Color.white]])
	}
	// sets all states
	label_ { arg string;
		view.states = view.states.collect({ arg st;
			st.put(0,string.asString);
			st
		});
	}
	// assumes 1 state
	background_ { arg color;
		var s;
		s = view.states;
		s.at(0).put(2,color);
		view.states = s;
		view.refresh;
	}
	labelColor_ { arg color;
		var s;
		s = view.states;
		s.at(0).put(1,color);
		view.states = s;
		view.refresh;
	}
	*defaultHeight { ^GUI.skin.buttonHeight }
}

// abreviation for a one state button
ActionButton : SCButtonAdapter {

	var <action;

	*new { arg layout,title,function,minWidth=20,minHeight,color,backcolor,font;
		^super.new.init(layout,title,function,minWidth,minHeight,color,backcolor,font)
	}
	init { arg layout,title,function,minWidth=20,minHeight,color,backcolor,font;
		var environment,optimalWidth;
		title = title.asString;
		if(title.size > 40,{ title = title.copyRange(0,40) });
		if(font.isNil,{ font = GUI.font.new(*GUI.skin.fontSpecs) });
		optimalWidth = title.bounds(font).width;
		this.makeViewWithStringSize(layout,optimalWidth,minWidth,minHeight);
		view.states_([[title,color ?? {Color.black},
			backcolor ?? {Color.new255(205, 201, 201)}]]);
		view.font_(font);
		view.action_(function);
		if(consumeKeyDowns,{ this.keyDownAction = {nil}; });
	}
}


ToggleButton : SCButtonAdapter {

	var <state,<>onFunction,<>offFunction;

	*new { arg layout,title,onFunction,offFunction,init=false,minWidth=20,minHeight;
			^super.new.init(layout,init, title,minWidth,minHeight)
				.onFunction_(onFunction).offFunction_(offFunction)
	}
	value { ^state }
	toggle { arg way,doAction = true;
		if(doAction,{
			this.prSetState(way ? state.not)
		},{
			state = way ? state.not;
		});
		view.setProperty(\value,state.binaryValue);
	}
	// private
	init { arg layout,init,title,minWidth,minHeight;
		var font;
		font = GUI.font.new(*GUI.skin.fontSpecs);
		this.makeViewWithStringSize(layout,title.bounds(font).width,minWidth,minHeight);
		view.states = [
			[title,GUI.skin.fontColor,GUI.skin.offColor],
			[title,GUI.skin.fontColor,GUI.skin.onColor]
		];
		state=init;
		view.value_(state.binaryValue);
		view.action_({this.prSetState(state.not)});
		view.font = font;
	}
	prSetState { arg newstate;
		state = newstate;
		if(state,{
			onFunction.value(this)
		},{
			(offFunction ? onFunction).value(this)
		});
	}
}
