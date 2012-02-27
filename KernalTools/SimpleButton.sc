

SimpleButton : SCViewHolder {

	var <action;

	*new { arg layout,bounds,title,action,color,background;
		^super.new.init(layout,bounds,title,action,color,background)
	}
	init { arg layout,bounds,title,action,color,background;
		var optimalWidth,skin,minWidth,minHeight,font;
		skin = GUI.skin;
		title = title.asString;
		font = GUI.font.new(*skin.fontSpecs);
		if(bounds.isNil,{
			optimalWidth = title.bounds(font).width;
		},{
			# minWidth,minHeight = bounds.asArray;
			optimalWidth = minWidth;
		});
		this.makeViewWithStringSize(layout,optimalWidth,minWidth ? optimalWidth,minHeight ? skin.buttonHeight);
		view.font = font;
		view.states_([[title,color ? skin.fontColor,background ? skin.background]]);
		view.action_(action);
		this.keyDownAction = {nil};
	}
	makeViewWithStringSize { arg layout,optimalWidth,minWidth,minHeight;
		this.makeView( layout,(optimalWidth + 10).max(minWidth?20),minHeight )
	}
	makeView { arg layout,x,y;
		var rect;
		if((layout.isNil or: { layout.isKindOf(PageLayout) }),{ layout = layout.asFlowView; });
		this.view = GUI.button.new(layout,Rect(0,0,x,y ? GUI.skin.buttonHeight));
		this.view.keyDownAction_({nil});
	}

	label_ { arg string;
		view.states = view.states.collect({ arg st;
			st.put(0,string.asString);
			st
		});
	}
	background_ { arg color;
		var s;
		s = view.states;
		s.at(0).put(2,color);
		view.states = s;
		view.refresh;
	}
}


SimpleLabel : SCViewHolder {

	classvar <>bgcolor;

	*new { arg layout,string,width,height,minWidth=15,font;
		var new;
		new = this.prNew(layout,string,width,height,minWidth,font);
		new.background_(Color(0.9843137254902, 0.9843137254902, 0.9843137254902, 1.0))
			.align_(\left);
		^new
	}
	*prNew { arg layout,string,width,height,minWidth,font;
		string = string.asString;
		if(font.isNil,{ font =  GUI.font.new(*GUI.skin.fontSpecs) });

		^super.new.init(layout, Rect(0,0,
				width ?? {(string.bounds(font).width + 6).max(minWidth)} ,
				height ?? {GUI.skin.buttonHeight}))
			.font_(font)
			.label_(string)
	}
	init { |layout, bounds, string|
		view = GUI.staticText.new(layout, bounds);
	}
	label_ { arg string;
		view.string_(" " ++ string ++ " ");
	}
	bold { arg fontSize=11;
		this.font = GUI.font.new("Helvetica-Bold",fontSize);
	}
}


ArgName : SimpleLabel {
	
	*new { arg name,layout,minWidth=130;
		^super.new(layout,name,minWidth: minWidth)
			.background_(Color( 0.47843137254902, 0.72941176470588, 0.50196078431373 ))
			.align_(\left)
	}
}

