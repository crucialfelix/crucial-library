
EditorGui : ObjectGui {
	writeName {}
}

NumberEditorGui : EditorGui {

	var numv,slv;

	smallGui { arg layout;
		var l;
		l=this.guify(layout);
		this.box(l,Rect(0,0,40,GUI.skin.buttonHeight));
		if(layout.isNil,{ l.front });
	}
	guiBody { arg layout,slider=true, box=true;
		var bounds,h;
		bounds = layout.indentedRemaining;

		// massive space,
			// box, slider horz
		if(bounds.width >= 140 and: {bounds.height >= 11},{
			h = min(bounds.height, GUI.skin.buttonHeight);
			if(box, { this.box(layout,Rect(0,0,40,h)); });
			if(slider,{ this.slider(layout,Rect(0,0,100,h)); });
			^this
		});
		// width < height
			// go vert
		if(bounds.width < bounds.height,{
			// height > 100
				// box, slider
			if(bounds.height > 100 and: {bounds.width >= 30},{
				layout.comp({ |l|
					var y;
					this.box(l,Rect(0,0,y = min(40,bounds.width),GUI.skin.buttonHeight));
					this.slider(l,Rect(0,y,min(30,bounds.width),h-GUI.skin.buttonHeight));
				},Rect(0,0,40,h = bounds.height.max(130)))
				^this
			});
			if(bounds.height > 100 ,{
				this.slider(layout,Rect(0,0,min(40,bounds.width),bounds.height.min(150)));
				^this
			});
			// height < 100, > 30
				// slider
			if(bounds.height >= 30,{
				this.slider(layout,Rect(0,0,min(40,bounds.width),bounds.height));
				^this
			});

			// height < 30
				// box
			if(bounds.height <= 30,{
				this.box(layout,Rect(0,0,min(40,bounds.width),bounds.height));
				^this
			});

		},{// width > height
			h = min(bounds.height, GUI.skin.buttonHeight);

			// width > 100
				// box, slider

			// width < 100, > 30
				// slider
			if(bounds.width.inclusivelyBetween(30,100),{
				if(slider,{
					this.slider(layout,Rect(0,0,bounds.width,h));
				},{
					this.box(layout,Rect(0,0,bounds.width,h));
				});
				^this
			});

			// width < 30
			if(bounds.width <= 30,{
				// box
				if(box,{
					this.box(layout,Rect(0,0,bounds.width,h));
				},{
					this.slider(layout,Rect(0,0,bounds.width,h));
				});
				^this
			});
		});

		// any unmatched
		if(slider,{
			this.slider(layout,Rect(0,0,bounds.width,h));
		},{
			this.box(layout,Rect(0,0,bounds.width,h));
		});
		^this
	}
	box { arg layout,bounds;
		var r,startValue,range,mod,startPoint;
		numv = NumberBox(layout,bounds)
			.object_(model.poll)
			.focusColor_(Color.yellow(1.0,0.5))
			.action_({ arg nb;
				model.activeValue_(model.spec.constrain(nb.value)).changed(numv);
			});
		numv.mouseDownAction = { arg view,x, y, modifiers, buttonNumber, clickCount;
			if(modifiers.isAlt,{
				model.activeValue_(model.spec.default).changed
			},{
				startValue = model.unmappedValue;
				mod = modifiers;
				startPoint = 0@0;
			})
		};
		numv.mouseMoveAction = { arg view,x,y,modifiers;
			var move,val,unimove;
			if(modifiers != mod,{
				mod = modifiers;
				startValue = model.unmappedValue;
				startPoint = x@y;
			});
			if(modifiers.isCtrl,{
			    move = (y - startPoint.y).neg;
				if(modifiers.isShift,{
					range = 1800.0;
				},{
					range = 300.0;
				});
				move = move.clip(range.neg,range);
				unimove = move.abs.linlin(0.0,range,0.0,1.0);
				if(move > 0,{
				    val = (startValue + unimove);
				},{
				    val = (startValue - unimove);
				});
				model.setUnmappedValue( val.clip(0.0,1.0) );
			});
		};
		numv.scroll = false;
		numv.clipLo = model.spec.minval;
		numv.clipHi = model.spec.maxval;
		
		/*numv.keyDownAction = { arg char,modifiers,unicode,keycode;
			if("012356789-.".includes(char),{
				this.defaultKeyDownAction(char, modifiers, unicode, keycode);
			},{
				nil
			})
		};*/
		//if(consumeKeyDowns,{
		//	numv.keyDownAction = {nil};
		//});
	}
	slider { arg layout, bounds;
		var r;
		slv = GUI.slider.new(layout, bounds);
		slv.focusColor_(Color.yellow(1.0,0.2));
		slv.setProperty(\value,model.spec.unmap(model.poll));
		slv.action_({arg th;
			model.activeValue_(model.spec.map(th.value)).changed(slv)
		});
		if(consumeKeyDowns,{ slv.keyDownAction = {nil}; });
	}
	update {arg changed,changer; // always has a number box
		{
			if(changer !== numv and: {numv.notNil},{
				numv.value_(model.poll);
			});
			if(changer !== slv and: {slv.notNil},{
				slv.value_(model.spec.unmap(model.poll));
			});
			nil
		}.defer;
	}
	background { ^Color(0.0,0.2,0.2,0.2) }
}

KrNumberEditorGui : NumberEditorGui {
	background { ^Color(0.0,0.3,0.0,0.2) }
}


PopUpEditorGui : EditorGui {
	var popV;
	    // temp, I don't really have a spec here
        // we arent editing a "pop up", so the class is misnamed
        // just to get this gui representation
        // maybe NumberEditor should use this gui if it has a named integers spec
	guiBody { arg layout;
		var horSize;
		horSize = model.labels.maxValue({arg item; item.size }) * 12;
		popV = PopUpMenu(layout,Rect(0,0,horSize,GUI.skin.buttonHeight))
			.items_(model.labels)
			.action_({ arg nb;
				model.selectByIndex(popV.value).changed(this)
			});
		popV.background = GUI.skin.background;
		if(consumeKeyDowns,{ popV.keyDownAction = {nil}; });
		popV.setProperty(\value,model.selectedIndex)
	}
	update { arg changed,changer;
		if(changer !== this,{
			popV.setProperty(\value,model.selectedIndex)
		});
	}
}



BooleanEditorGui : EditorGui {
	var cb;
	guiBody { arg layout,bounds;
		var bg,b;
		bg = Color.clear;
		if(bounds.isNil,{ bounds = layout.bounds; });

		b = Rect(0,0,14,14);
		if(bounds.notNil,{
			if(b.width > bounds.width,{
				b.width= bounds.width;
				b.height = bounds.width;
			});
			if(b.height > bounds.height,{
				b.width = bounds.height;
				b.height = bounds.height;
			});
		});
		cb = GUI.button.new( layout,b);
		cb.states = [[" ",bg,bg],["X",Color.black,bg]];
		cb.font = GUI.font.new("Helvetica",9);
		cb.setProperty(\value,model.value.binaryValue);
		cb.action = { model.activeValue_(cb.value != 0,this) };
		if(consumeKeyDowns,{ cb.keyDownAction = {nil}; });
	}
	update { arg changed,changer;
		if(changer !== this,{
			cb.setProperty(\value,model.value.binaryValue);
		});
	}
}

