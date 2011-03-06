
InstrGui : ObjectGui {
	
	guiBody { arg layout;
		var defs,h,w,specWidth;
        var width;

        width = min(layout.indentedRemaining.width,600);
		specWidth = width - 150 - 100 - 6;

		model.argNames.do({ arg a,i;
			layout.startRow;
			ArgNameLabel(  a ,layout,150);
			CXLabel(layout, " = " ++ model.defArgAt(i).asString,100);
			model.specs.at(i).asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);
		});
		layout.startRow;
		CXLabel(layout,"outSpec:",150);
		CXLabel(layout, model.outSpec.asString, 100);
		model.outSpec.asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);

		if(model.path.notNil,{
			CXLabel(layout.startRow,model.path,width);
		});

		layout.startRow;
		if(model.path.notNil and: { File.exists(model.path) },{
			ActionButton(layout,"open file",{ model.path.openTextFile });
		});
		ActionButton(layout,"make a Patch",{ Patch(model.dotNotation).topGui });
		ActionButton(layout,"post Instr name",{
		    model.dotNotation.post;
		}).beginDragAction = {model.dotNotation};

        this.sourceGui(layout,width);
	}
	sourceGui { arg layout,width;
	    var source,up;
		var tf,lines,height,f;
	    
		layout.startRow;
		source = model.funcDef.sourceCode;
		if(source.notNil,{
			f = GUI.font.new("Courier",12.0);
			height = source.bounds(f).height + 5;
			tf = TextView(layout,Rect(0,0,width,height));
			tf.string = source;
			tf.font_(f);
			tf.syntaxColorize;
		},{
		    CXLabel(layout,"Source code not found",width);
		});
		up = Updater(model,{
		    source = model.funcDef.sourceCode;
		    if(tf.isClosed,{
		        up.remove //sc remove gui is easily breakable
		    },{
			    tf.string = source;
			    tf.syntaxColorize;
			});
        }).removeOnClose(layout)
	}
}

UGenInstrGui : InstrGui {
	
	sourceGui { arg layout;
	    ActionButton(layout,"Help",{
	        model.ugenClass.openHelpFile
	    });
	}
	
}

