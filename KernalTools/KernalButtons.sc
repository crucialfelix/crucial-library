

ClassNameLabel : ActionButton {

	*new { arg  class,layout,minWidth=200,minHeight;
		^super.new(layout,class.name.asString,{class.ginsp},minWidth,minHeight,
			Color.white,
			Color( 0.52156862745098, 0.75686274509804, 0.90196078431373 )
		);
	}
	*newBig { arg  class,layout,minWidth=200,minHeight=30;
		^super.new(layout,class.name.asString,{class.ginsp},minWidth,minHeight, Color.white,
			Color( 0.52156862745098, 0.75686274509804, 0.90196078431373 ),
			GUI.font.new("Helvetica-Bold",18))
	}
}


MethodLabel : ActionButton {
	// show args and prototypes
	*new { arg  method,layout,minWidth=100;
		^super.new(layout,method.ownerClass.name.asString ++ "-" ++ method.name.asString,
			{method.ginsp},minWidth,GUI.skin.buttonHeight,nil,Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
	*withoutClass { arg  method,layout,minWidth=100;
		^super.new(layout, method.name.asString,{method.ginsp},minWidth,GUI.skin.buttonHeight,nil,
			Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
	*classMethod { arg  method,layout,minWidth=100;
		^super.new(layout,"*" ++ method.name.asString,{method.ginsp},minWidth,GUI.skin.buttonHeight,nil,
			Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
}


SelectorLabel : ActionButton {

	*new { arg  selector,layout,minWidth=100; // can send it to a MethodBrowser
		^super.new(layout,selector.asString,nil,minWidth,nil,
			nil,Color( 0.74509803921569, 0.8078431372549, 0.57647058823529 ));
	}
}


Tile : ActionButton { // the name comes from Squeak

	*new { arg  target,layout,minWidth=100;
		if(target.guiClass == StringGui,{
			^target.gui(layout);
		});
		^super.new(layout,target.asString,{
				target.gui;
				//#F6F9F5
			},minWidth,GUI.skin.buttonHeight, Color.black,			Color.new255(248, 248, 255))
	}

}


InspectorLink : ActionButton {
	
	*new { arg  target,layout,minWidth=150;
		^super.new(layout,target.asString,{target.insp},minWidth,nil,
			Color.new255(70, 130, 200),
			Color.white,
			GUI.font.new("Helvetica",12)
		)
	}
	*big { arg  target,layout,minWidth=200;
		^super.new(layout,target.asString,{target.insp},minWidth,30,
			Color.black,Color.white,GUI.font.new("Helvetica-Bold",18))
	}
	*icon { arg target,layout;
		^GUI.button.new(layout,Rect(0,0,30,GUI.skin.buttonHeight))
			.action_({ target.insp })
			.states_([["insp",Color.black,Color.white]])
			.font_(GUI.font.new("Helvetica",9));
	}
	*captioned { arg caption,target,layout,minWidth=150;
		CXLabel(layout,caption,minWidth:minWidth);
		this.new(target,layout);
	}
}




