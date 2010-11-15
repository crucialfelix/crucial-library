+ Nil {
	asPageLayout { arg name,bounds,metal=true;
		^MultiPageLayout(name.asString,bounds, metal: metal ).front
	}
}

+ Point {
	asPageLayout {
		^MultiPageLayout("",this.asRect ).front
	}
}
+ Rect {
	asPageLayout {
		^MultiPageLayout("",this ).front
	}
}

+ MultiPageLayout {
	asFlowView { arg bounds;
		^if(bounds.notNil,{
			FlowView(this,bounds)
		},{
			this.view
		})

		//bounds = bounds ?? {this.view.bounds};
		//^FlowView(this.view,this.layRight(bounds.width - 10,bounds.height - 10))
	}
}

+ MultiPageLayout {

	flow { arg func,bounds;
		^this.view.flow(func,bounds)
	}
	vert { arg func,bounds,spacing;
		^this.view.vert(func,bounds,spacing)
	}
	horz { arg func,bounds,spacing;
		^this.view.horz(func,bounds,spacing)
	}
	comp { arg func,bounds;
		^this.view.comp(func,bounds)
	}
	scroll { arg ... args;
		^this.view.performList(\scroll,args)
	}
}
