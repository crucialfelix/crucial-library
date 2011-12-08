
// pending deprecation
MultiPageLayout : PageLayout { }

Sheet {
	*new { arg buildDialog,name="",bounds;
		var layout;
		layout = PageLayout(name,bounds,front:false);
		buildDialog.value(layout);
		layout.resizeToFit(center:true);
		layout.front;
		^layout
	}
}

ModalDialog { // hit ok or cancel

	*new { arg buildDialog,okFunc,name="?",cancelFunc;
		var globalKeyDownFunc;
		globalKeyDownFunc = SCView.globalKeyDownAction;
		SCView.globalKeyDownAction = nil;

		Sheet({ arg layout;
			var returnObjects;

			returnObjects=buildDialog.value(layout);

			layout.startRow;
			ActionButton(layout,"OK",{
				okFunc.value(returnObjects);
				layout.close;
			});

			ActionButton(layout,"Cancel",{
				cancelFunc.value(returnObjects);
				layout.close;
			});

		},name).onClose_({ SCView.globalKeyDownAction = globalKeyDownFunc; });
	}

}


