
InstrBrowser {

    var <>toolbarFunc,lv,frame;
    var instrs,ugenInstrs,<rate=nil,<>showUGenInstr=true;

    *new { arg toolbarFunc,showUGenInstr=true;
        //Instr.loadAll;
        ^super.newCopyArgs(toolbarFunc).showUGenInstr_(showUGenInstr).init.initUGenInstrs
    }
    gui { arg layout,bounds;
        this.guiBody( layout.asFlowView(bounds ?? {Rect(100,0,1000,1000)} ) );
    }
    guiBody { arg layout;
        var search,rateFilter;
        search = TextField(layout,Rect(0,0,240,17));
        search.string = "";
        search.keyDownAction = {true};
        search.action = {this.search(search.value)};

        rateFilter = PopUpMenu(layout,120@17);
        rateFilter.items = ["all","audio","control","fft"];
        rateFilter.action = {
            if(rateFilter.value == 0,{
                 this.rate = nil
            },{
                this.rate = rateFilter.item.asSymbol;
            })
        };
        layout.horz({ arg layout;
            lv = ListView(layout,250@layout.bounds.height);
            lv.items = this.allInstr;
            lv.mouseUpAction_({ arg view, x, y, modifiers, buttonNumber, clickCount;
                frame.removeAll;
                this.focus(lv.items[lv.value]);
            });
            lv.beginDragAction = {
                lv.items[lv.value]
            };
            frame = layout.flow({ arg layout; },(layout.bounds.width-200)@layout.bounds.height);
        },layout.bounds.width.max(900)@(layout.bounds.height - 100))
        .background_(Color(0.83582089552239, 0.83582089552239, 0.83582089552239));

        Updater(Instr,{
            this.init
        }).removeOnClose(layout);
    }
    init {
        instrs = Instr.leaves;
        if(rate.notNil,{
            instrs = instrs.select({ arg ins; ins.outSpec.notNil and: {ins.outSpec.rate == rate}})
        });
        instrs = instrs.collect(_.dotNotation).sort;
    }
    initUGenInstrs {
        ugenInstrs = UGenInstr.leaves(this.rateMethod); // should use rate not demand
        ugenInstrs = ugenInstrs.collect(_.name).sort;
    }
    rate_ { arg rr;
        rate = rr;
        this.init;
        this.initUGenInstrs;
        this.search("");
    }
    rateMethod {
        ^rate.switch(
                nil,\ar,
                \audio,\ar,
                \control,\kr,
                \demand,\new,
                \fft,\new
                );
    }
    allInstr {
        if(showUGenInstr,{
            ^(instrs ++ ugenInstrs);
        },{
            ^instrs;
        });
    }
    search { arg q;
        var base;
        if(q != "",{
            lv.items = (this.allInstr.select(_.containsi(q)));
        },{
            lv.items = this.allInstr
        });
        lv.refresh;
    }
    focus { arg instrname;
        var instr,ic,rr;
        if(instrname.isNil,{
            ^this
        });
        ic = instrname.asSymbol.asClass;
        if(ic.notNil and: {Instr.at([ic.name.asString]).isNil}) {
            rr = this.rateMethod;
            instr = UGenInstr(ic,rr)
        }{
            instr = Instr(instrname);
        };
        toolbarFunc.value(frame,instr);
        frame.startRow;
        frame.scroll({ arg frame;
            instr.gui(frame);
        });
    }
}


