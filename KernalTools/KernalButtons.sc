

ClassNameLabel : SimpleButton {

	*new { arg  class,layout,minWidth=200,minHeight;
		^super.new(layout,[minWidth,minHeight],
			class.name.asString,{class.ginsp},
			Color.white,
			Color( 0.52156862745098, 0.75686274509804, 0.90196078431373 )
		);
	}
	*newBig { arg  class,layout,minWidth=200,minHeight=30;
		^super.new(layout,[minWidth,minHeight],
			class.name.asString,{class.ginsp},
			Color.white,
			Color( 0.52156862745098, 0.75686274509804, 0.90196078431373 ),
			GUI.font.new("Helvetica-Bold",18))
	}
}


MethodLabel : SimpleButton {

	*new { arg  method,layout,minWidth=300;
		^super.new(layout,minWidth,method.ownerClass.name.asString ++ "-" ++ method.name.asString,
			{method.ginsp},nil,Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
	*withoutClass { arg  method,layout,minWidth=100;
		^super.new(layout,minWidth, method.name.asString,{method.ginsp},nil,
			Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
	*classMethod { arg  method,layout,minWidth=100;
		^super.new(layout,minWidth,"*" ++ method.name.asString,{method.ginsp},nil,
			Color.new255(245, 222, 179),GUI.font.new("Monaco",9));
	}
}


InspectorLink : SimpleButton {

	*new { arg  target,layout,minWidth=150;
		^super.new(layout,minWidth,target.asString,{target.insp; InspManager.front; },
			Color.new255(70, 130, 200),
			Color.white,
			GUI.font.new("Helvetica",12)
		)
	}
	*big { arg  target,layout,minWidth=200;
		^super.new(layout,minWidth@30,target.asString,{target.insp; InspManager.front; },
			Color.black,Color.white,GUI.font.new("Helvetica-Bold",18))
	}
	*icon { arg target,layout;
		^GUI.button.new(layout,Rect(0,0,30,GUI.skin.buttonHeight))
			.action_({ target.insp; InspManager.front })
			.states_([["insp",Color.black,Color.white]]);
	}
	*captioned { arg caption,target,layout,minWidth=150;
		SimpleLabel(layout,caption,minWidth:minWidth);
		this.new(target,layout);
	}
}


DefNameLabel {

	*new { arg name,server,layout,minWidth=130;
		var def;
		if(InstrSynthDef.notNil,{
			def = InstrSynthDef.cacheAt(name,server);
		});
		if(def.isNil,{
			^SimpleLabel(layout,name,minWidth:minWidth)
		},{
			^InspectorLink(def,layout,minWidth:minWidth)
		})
	}
}


VariableNameLabel : CXAbstractLabel {

	*new { arg name,layout,minWidth=120;
		^super.new(layout,name,minWidth: minWidth)
			.background_(Color( 1, 0.86666666666667, 0.38039215686275 ))
			.align_(\right)
	}
}



