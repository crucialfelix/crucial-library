+ Nil {
	asPageLayout { arg name,bounds;
		^PageLayout(name.asString,bounds ).front
	}
}

+ Point {
	asPageLayout {
		^PageLayout("",this.asRect ).front
	}
}
+ Rect {
	asPageLayout {
		^PageLayout("",this ).front
	}
}

+ PageLayout {
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

+ PageLayout {

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
