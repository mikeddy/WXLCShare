private BaseHandler bh = new BaseHandler(this);

	static class BaseHandler extends Handler {
		WeakReference<BaseActivity> act;

		public BaseHandler(BaseActivity ba) {
			super();
			act = new WeakReference<BaseActivity>(ba);
		}

		@Override
		public void handleMessage(Message msg) {
			if (act != null) {
				act.get().onMessage(msg);
			}
		}
	}

	public abstract void onMessage(Message msg);

	@Override
	public void onMessage(Message msg) {
		switch (msg.what) {
		case 1:
			versionTextView.setVisibility(View.GONE);
			break;
		}

	}