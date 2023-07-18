/*
 * Copyright (C) Globalegrow E-Commerce Co. , Ltd. 2007-2018.
 * All rights reserved.
 * This software is the confidential and proprietary information
 * of Globalegrow E-Commerce Co. , Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Globalegrow.
 */

package com.fz.multistateview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.Keep;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 包含6种不同状态的视图：
 * {@link #VIEW_STATE_CONTENT}内容，数据呈现区
 * {@link #VIEW_STATE_ERROR}错误，包括网络错误或者其他请求错误
 * {@link #VIEW_STATE_PROCESS}处理中，如果显示该布局，且改布局背景透明，则可见内容区
 * {@link #VIEW_STATE_EMPTY}空白,显示无数据时的内容
 * {@link #VIEW_STATE_LOADING}加载。
 * {@link #VIEW_STATE_NO_NETWORK}无网络时，一般可与错误一致，则不必增加，特殊时可单独处理<br>
 * 每个状态都有自己独立的布局，可以通过设置显示/隐藏
 * 相应地{@link ViewState}
 * 每个MultiStateView <b> <i>必须</ i> </ b>包含内容视图。 内容视图
 * 是通过XML声明从视图标记内部获取的
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/1/15 14:03
 */
public class MultiStateView extends FrameLayout implements NestedScrollingParent3,
        NestedScrollingChild3, NestedScrollingChild {
    public static String TAG = "MultiStateView";
    private static final String TAG_EMPTY = "empty";
    private static final String TAG_LOADING = "loading";
    private static final String TAG_ERROR = "error";
    private static final String TAG_NO_NETWORK = "noNetwork";
    private static final String TAG_PROCESS = "process";

    public static final int VIEW_STATE_UNKNOWN = -1;

    public static final int VIEW_STATE_CONTENT = 0;

    public static final int VIEW_STATE_ERROR = 1;

    public static final int VIEW_STATE_EMPTY = 2;

    public static final int VIEW_STATE_LOADING = 3;
    public static final int VIEW_STATE_NO_NETWORK = 4;
    public static final int VIEW_STATE_PROCESS = 5;
    private int mLoadingViewResId = NO_ID;
    private int mEmptyViewResId = NO_ID;
    private int mErrorViewResId = NO_ID;
    private int mNoNetworkViewResId = NO_ID;
    private int mProcessViewResId = NO_ID;
    /**
     * 是否在显示其他视图（如：{@link #VIEW_STATE_LOADING}等状态）的同时显示内容视图
     */
    private boolean isForceShowContent = false;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_STATE_UNKNOWN, VIEW_STATE_CONTENT, VIEW_STATE_ERROR, VIEW_STATE_EMPTY, VIEW_STATE_LOADING, VIEW_STATE_NO_NETWORK, VIEW_STATE_PROCESS})
    public @interface ViewState {
    }

    private LayoutInflater mInflater;

    private View mContentView;

    private View mLoadingView;

    private View mErrorView;

    private View mEmptyView;
    private View mNoNetworkView;
    /**
     * 正在处理中视图，比如购物车修改数量时请求网络
     */
    private View mProcessView;
    private boolean mAnimateViewChanges = false;

    @Nullable
    private StateListener mListener;

    @ViewState
    private int mViewState = VIEW_STATE_UNKNOWN;
    private int mLastY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    private NestedScrollingChildHelper mChildHelper;
    private NestedScrollingParentHelper mParentHelper;

    public MultiStateView(Context context, View contentView) {
        super(context);
        init(null, context);
        setContentView(contentView);
    }

    public MultiStateView(Context context) {
        super(context);
        init(null, context);
    }

    public MultiStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public MultiStateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, context);
    }

    private void init(AttributeSet attrs, Context context) {
        mChildHelper = new NestedScrollingChildHelper(this);
        mParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);
        mInflater = LayoutInflater.from(context);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStateView);

            mLoadingViewResId = a.getResourceId(R.styleable.MultiStateView_msv_loadingView, NO_ID);

            mEmptyViewResId = a.getResourceId(R.styleable.MultiStateView_msv_emptyView, NO_ID);
            isForceShowContent = a.getBoolean(R.styleable.MultiStateView_msv_forceShowContent, false);
            mErrorViewResId = a.getResourceId(R.styleable.MultiStateView_msv_errorView, NO_ID);
            mNoNetworkViewResId = a.getResourceId(R.styleable.MultiStateView_msv_noNetworkView, NO_ID);
            mProcessViewResId = a.getResourceId(R.styleable.MultiStateView_msv_processView, NO_ID);
            int viewState = a.getInt(R.styleable.MultiStateView_msv_viewState, VIEW_STATE_CONTENT);
            mAnimateViewChanges = a.getBoolean(R.styleable.MultiStateView_msv_animateViewChanges, false);
            switch (viewState) {
                case VIEW_STATE_CONTENT:
                    mViewState = VIEW_STATE_CONTENT;
                    break;

                case VIEW_STATE_ERROR:
                    mViewState = VIEW_STATE_ERROR;
                    break;

                case VIEW_STATE_EMPTY:
                    mViewState = VIEW_STATE_EMPTY;
                    break;

                case VIEW_STATE_LOADING:
                    mViewState = VIEW_STATE_LOADING;
                    break;
                case VIEW_STATE_PROCESS:
                    mViewState = VIEW_STATE_PROCESS;
                    break;
                case VIEW_STATE_UNKNOWN:
                default:
                    mViewState = VIEW_STATE_UNKNOWN;
                    break;
            }
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContentView == null) throw new IllegalArgumentException("Content view is not defined");
        setView(VIEW_STATE_UNKNOWN);
    }

    /**
     * 添加一个状态布局
     *
     * @param status 状态类型
     * @param view   需要添加的布局
     * @author dingpeihua
     * @date 2021/1/19 18:17
     * @version 1.0
     */
    public void addView(@ViewState int status, View view) {
        setViewForState(view, status);
    }

    /**
     * 添加一个状态布局
     *
     * @param status 状态类型
     * @param rid    布局id
     * @author dingpeihua
     * @date 2021/1/19 18:17
     * @version 1.0
     */
    public void addView(@ViewState int status, @LayoutRes int rid) {
        View view = LayoutInflater.from(getContext()).inflate(rid, this, false);
        addView(status, view);
    }

    public void setLoadingViewResId(int loadingViewResId) {
        this.mLoadingViewResId = loadingViewResId;
    }

    public void setEmptyViewResId(int emptyViewResId) {
        this.mEmptyViewResId = emptyViewResId;
    }

    public void setErrorViewResId(int errorViewResId) {
        this.mErrorViewResId = errorViewResId;
    }

    public void setNoNetworkViewResId(int noNetworkViewResId) {
        this.mNoNetworkViewResId = noNetworkViewResId;
    }

    public void setProcessViewResId(int processViewResId) {
        this.mProcessViewResId = processViewResId;
    }

    @Override
    public void removeView(View view) {
        if (view != null) {
            super.removeView(view);
        }
    }

    public void setContentView(View mContentView) {
        removeView(this.mContentView);
        this.mContentView = mContentView;
        addView(mContentView);
    }

    public void setLoadingView(View mLoadingView) {
        removeView(this.mLoadingView);
        this.mLoadingView = mLoadingView;
        addView(mLoadingView);
    }

    public void setErrorView(View mErrorView) {
        removeView(this.mErrorView);
        this.mErrorView = mErrorView;
        addView(mErrorView);
    }

    public void setEmptyView(View mEmptyView) {
        removeView(this.mEmptyView);
        this.mEmptyView = mEmptyView;
        addView(mEmptyView);
    }

    public void setNoNetworkView(View mNoNetworkView) {
        removeView(this.mNoNetworkView);
        this.mNoNetworkView = mNoNetworkView;
        addView(mNoNetworkView);
    }

    public void setProcessView(View mProcessView) {
        removeView(this.mProcessView);
        this.mProcessView = mProcessView;
        addView(mProcessView);
    }

    public boolean hasContentView() {
        return mContentView != null;
    }

    public boolean hasProcessView() {
        return mProcessView != null || mProcessViewResId != NO_ID;
    }

    public boolean hasNoNetworkView() {
        return mNoNetworkView != null || mNoNetworkViewResId != NO_ID;
    }

    public boolean hasEmptyView() {
        return mEmptyView != null || mEmptyViewResId != NO_ID;
    }

    public boolean hasErrorView() {
        return mErrorView != null || mErrorViewResId != NO_ID;
    }

    public boolean hasLoadingView() {
        return mLoadingView != null || mLoadingViewResId != NO_ID;
    }

    /**
     * 是否在显示其他视图（如：loading  error等）的同时显示内容视图
     *
     * @param forceShowContent true表示强制
     * @author dingpeihua
     * @date 2020/3/20 15:11
     * @version 1.0
     */
    public void setForceShowContent(boolean forceShowContent) {
        isForceShowContent = forceShowContent;
    }

    private void contentViewVisibility(boolean isShow) {
        if (mContentView != null) {
            mContentView.setVisibility(isForceShowContent || isShow ? VISIBLE : GONE);
        }
    }

    /**
     * All of the addView methods have been overridden so that it can obtain the content view via XML
     * It is NOT recommended to add views into MultiStateView via the addView methods, but rather use
     * any of the setViewForState methods to set views for their given ViewState accordingly
     */
    @Override
    public void addView(View child) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    /**
     * Returns the {@link View} associated with the {@link ViewState}
     *
     * @param state The {@link ViewState} with to return the view for
     * @return The {@link View} associated with the {@link ViewState}, null if no view is present
     */
    @Nullable
    public View getView(@ViewState int state) {
        switch (state) {
            case VIEW_STATE_LOADING:
                ensureLoadingView();
                return mLoadingView;
            case VIEW_STATE_CONTENT:
                return mContentView;
            case VIEW_STATE_EMPTY:
                ensureEmptyView();
                return mEmptyView;
            case VIEW_STATE_ERROR:
                ensureErrorView();
                return mErrorView;
            case VIEW_STATE_NO_NETWORK:
                ensureNoNetWorkView();
                return mNoNetworkView;
            case VIEW_STATE_PROCESS:
                ensureProcessView();
                return mProcessView;
            case VIEW_STATE_UNKNOWN:
            default:
                return null;
        }
    }

    private void ensureProcessView() {
        if (mProcessView == null && mProcessViewResId != NO_ID) {
            mProcessView = mInflater.inflate(mProcessViewResId, this, false);
            mProcessView.setTag(R.id.tag_multi_state_view, TAG_NO_NETWORK);
            addView(mProcessView, mProcessView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_PROCESS, mProcessView);

            if (mViewState != VIEW_STATE_NO_NETWORK) {
                mProcessView.setVisibility(GONE);
            }
        }
    }

    public View getProcessView() {
        return getView(VIEW_STATE_PROCESS);
    }

    public View getNoNetworkView() {
        return getView(VIEW_STATE_NO_NETWORK);
    }

    public View getContentView() {
        return getView(VIEW_STATE_CONTENT);
    }

    public View getLoadingView() {
        return getView(VIEW_STATE_LOADING);
    }

    public View getErrorView() {
        return getView(VIEW_STATE_ERROR);
    }

    public View getEmptyView() {
        return getView(VIEW_STATE_EMPTY);
    }

    private void ensureNoNetWorkView() {
        if (mNoNetworkView == null && mNoNetworkViewResId != NO_ID) {
            mNoNetworkView = mInflater.inflate(mNoNetworkViewResId, this, false);
            mNoNetworkView.setTag(R.id.tag_multi_state_view, TAG_NO_NETWORK);
            addView(mNoNetworkView, mNoNetworkView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_NO_NETWORK, mNoNetworkView);

            if (mViewState != VIEW_STATE_NO_NETWORK) {
                mNoNetworkView.setVisibility(GONE);
            }
        }
    }

    /**
     * Returns the current {@link ViewState}
     *
     * @return
     */
    @ViewState
    public int getViewState() {
        return mViewState;
    }

    /**
     * Sets the current {@link ViewState}
     *
     * @param state The {@link ViewState} to set {@link MultiStateView} to
     */
    public void setViewState(@ViewState int state) {
        if (state != mViewState) {
            int previous = mViewState;
            mViewState = state;
            setView(previous);
            if (mListener != null) mListener.onStateChanged(mViewState);
        }
    }

    public boolean isContentView() {
        return mViewState == VIEW_STATE_CONTENT;
    }

    public boolean isLoadingView() {
        return mViewState == VIEW_STATE_LOADING;
    }

    public boolean isEmptyView() {
        return mViewState == VIEW_STATE_EMPTY;
    }

    public boolean isErrorView() {
        return mViewState == VIEW_STATE_ERROR;
    }

    public boolean isNoNetworkView() {
        return mViewState == VIEW_STATE_NO_NETWORK;
    }

    public boolean isProcessView() {
        return mViewState == VIEW_STATE_PROCESS;
    }

    /**
     * 监听按下返回键
     *
     * @author dingpeihua
     * @date 17-4-30 上午9:58
     * @version 1.0
     */
    public boolean onBackPressed() {
        if (isProcessView()) {
            showContentView();
            return true;
        }
        return false;
    }

    /**
     * 显示没有网络视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:52
     * @version 1.0
     */
    public void showNoNetworkView() {
        setViewState(VIEW_STATE_NO_NETWORK);
    }

    /**
     * 显示加载中视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:52
     * @version 1.0
     */
    public void showLoadingView() {
        setViewState(VIEW_STATE_LOADING);
    }

    /**
     * 显示空数据视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:52
     * @version 1.0
     */
    public void showEmptyView() {
        setViewState(VIEW_STATE_EMPTY);
    }

    /**
     * 显示数据视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:53
     * @version 1.0
     */
    public void showContentView() {
        setViewState(VIEW_STATE_CONTENT);
    }

    /**
     * 显示错误视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:53
     * @version 1.0
     */
    public void showErrorView() {
        setViewState(VIEW_STATE_ERROR);
    }

    /**
     * 显示正在处理中视图
     *
     * @author dingpeihua
     * @date 2017/3/17 15:53
     * @version 1.0
     */
    public void showProcessView() {
        setViewState(VIEW_STATE_PROCESS);
    }

    /**
     * Shows the {@link View} based on the {@link ViewState}
     */
    private void setView(@ViewState int previousState) {
        switch (mViewState) {
            case VIEW_STATE_LOADING:
                ensureLoadingView();
                if (mLoadingView == null) {
                    throw new NullPointerException("Loading View");
                }
                contentViewVisibility(false);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);
                if (mProcessView != null) mProcessView.setVisibility(View.GONE);
                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mLoadingView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_EMPTY:

                ensureEmptyView();

                if (mEmptyView == null) {
                    throw new NullPointerException("Empty View");
                }


                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                contentViewVisibility(false);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);
                if (mProcessView != null) mProcessView.setVisibility(View.GONE);
                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_ERROR:

                ensureErrorView();

                if (mErrorView == null) {
                    throw new NullPointerException("Error View");
                }


                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                contentViewVisibility(false);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);
                if (mProcessView != null) mProcessView.setVisibility(View.GONE);
                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mErrorView.setVisibility(View.VISIBLE);
                }
                break;
            case VIEW_STATE_NO_NETWORK:
                ensureNoNetWorkView();
                if (mNoNetworkView == null) {
                    throw new NullPointerException("No Network View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                contentViewVisibility(false);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mProcessView != null) mProcessView.setVisibility(View.GONE);
                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mNoNetworkView.setVisibility(View.VISIBLE);
                }
                break;
            case VIEW_STATE_PROCESS:
                ensureProcessView();
                if (mProcessView == null) {
                    throw new NullPointerException("No Network View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                contentViewVisibility(true);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mProcessView.setVisibility(View.VISIBLE);
                }
                break;
            case VIEW_STATE_CONTENT:
            default:
                if (mContentView == null) {
                    // Should never happen, the view should throw an exception if no content view is present upon creation
                    throw new NullPointerException("Content View");
                }

                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mProcessView != null) mProcessView.setVisibility(View.GONE);
                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    contentViewVisibility(true);
                }
                break;
        }
    }

    /**
     * Checks if the given {@link View} is valid for the Content View
     *
     * @param view The {@link View} to check
     * @return
     */
    private boolean isValidContentView(View view) {
        if (mContentView != null && mContentView != view) {
            return false;
        }
        Object tag = view.getTag(R.id.tag_multi_state_view);
        if (tag == null) {
            return true;
        }
        if (tag instanceof String) {
            String viewTag = (String) tag;
            if (TextUtils.equals(viewTag, TAG_EMPTY)
                    || TextUtils.equals(viewTag, TAG_ERROR)
                    || TextUtils.equals(viewTag, TAG_LOADING)
                    || TextUtils.equals(viewTag, TAG_PROCESS)
                    || TextUtils.equals(viewTag, TAG_NO_NETWORK)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param id the id of the view to be found
     * @return the view of the specified id, null if cannot be found
     */
    @Keep
    protected <T extends View> T findViewTraversal(@IdRes int id) {
        if (id == getId()) {
            return (T) this;
        }
        final int len = getChildCount();
        for (int i = 0; i < len; i++) {
            View v = getChildAt(i);
            v = v.findViewById(id);
            if (v != null) {
                return (T) v;
            }
        }
        View view = null;
        if (mContentView != null) {
            view = mContentView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        ensureNoNetWorkView();
        if (mNoNetworkView != null) {
            view = mNoNetworkView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        ensureEmptyView();
        if (mEmptyView != null) {
            view = mEmptyView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        ensureErrorView();
        if (mErrorView != null) {
            view = mErrorView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        ensureProcessView();
        if (mProcessView != null) {
            view = mProcessView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        ensureLoadingView();
        if (mLoadingView != null) {
            view = mLoadingView.findViewById(id);
        }
        if (view != null) {
            return (T) view;
        }
        return null;
    }

    /**
     * Sets the view for the given view state
     *
     * @param view          The {@link View} to use
     * @param state         The {@link ViewState}to set
     * @param switchToState If the {@link ViewState} should be switched to
     */
    public void setViewForState(View view, @ViewState int state, boolean switchToState) {
        switch (state) {
            case VIEW_STATE_LOADING:
                if (mLoadingView != null) removeView(mLoadingView);
                mLoadingView = view;
                mLoadingView.setTag(R.id.tag_multi_state_view, TAG_LOADING);
                addView(mLoadingView);
                break;

            case VIEW_STATE_EMPTY:
                if (mEmptyView != null) removeView(mEmptyView);
                mEmptyView = view;
                mEmptyView.setTag(R.id.tag_multi_state_view, TAG_EMPTY);
                addView(mEmptyView);
                break;

            case VIEW_STATE_ERROR:
                if (mErrorView != null) removeView(mErrorView);
                mErrorView = view;
                mErrorView.setTag(R.id.tag_multi_state_view, TAG_ERROR);
                addView(mErrorView);
                break;
            case VIEW_STATE_NO_NETWORK:
                if (mNoNetworkView != null) removeView(mNoNetworkView);
                mNoNetworkView = view;
                mNoNetworkView.setTag(R.id.tag_multi_state_view, TAG_NO_NETWORK);
                addView(mNoNetworkView);
                break;
            case VIEW_STATE_PROCESS:
                if (mProcessView != null) removeView(mProcessView);
                mProcessView = view;
                mProcessView.setTag(R.id.tag_multi_state_view, TAG_PROCESS);
                addView(mProcessView);
                break;
            case VIEW_STATE_CONTENT:
                if (mContentView != null) removeView(mContentView);
                mContentView = view;
                addView(mContentView);
                break;
            default:
                break;
        }

        setView(VIEW_STATE_UNKNOWN);
        if (switchToState) setViewState(state);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param view  The {@link View} to use
     * @param state The {@link ViewState} to set
     */
    public void setViewForState(View view, @ViewState int state) {
        setViewForState(view, state, false);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param layoutRes     Layout resource id
     * @param state         The {@link ViewState} to set
     * @param switchToState If the {@link ViewState} should be switched to
     */
    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state, boolean switchToState) {
        if (mInflater == null) mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(layoutRes, this, false);
        setViewForState(view, state, switchToState);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param layoutRes Layout resource id
     * @param state     The {@link View} state to set
     */
    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state) {
        setViewForState(layoutRes, state, false);
    }

    /**
     * Sets whether an animate will occur when changing between {@link ViewState}
     *
     * @param animate
     */
    public void setAnimateLayoutChanges(boolean animate) {
        mAnimateViewChanges = animate;
    }

    /**
     * Sets the {@link StateListener} for the view
     *
     * @param listener The {@link StateListener} that will receive callbacks
     */
    public void setStateListener(StateListener listener) {
        mListener = listener;
    }

    /**
     * Animates the layout changes between {@link ViewState}
     *
     * @param previousView The view that it was currently on
     */
    private void animateLayoutChange(@Nullable final View previousView) {
        if (previousView == null) {
            getView(mViewState).setVisibility(View.VISIBLE);
            return;
        }

        previousView.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(previousView, "alpha", 1.0f, 0.0f).setDuration(250L);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                previousView.setVisibility(View.GONE);
                getView(mViewState).setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(getView(mViewState), "alpha", 0.0f, 1.0f).setDuration(250L).start();
            }
        });
        anim.start();
    }

    private void ensureLoadingView() {
        if (mLoadingView == null && mLoadingViewResId != NO_ID) {
            mLoadingView = mInflater.inflate(mLoadingViewResId, this, false);
            mLoadingView.setTag(R.id.tag_multi_state_view, TAG_LOADING);
            addView(mLoadingView, mLoadingView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_LOADING, mLoadingView);

            if (mViewState != VIEW_STATE_LOADING) {
                mLoadingView.setVisibility(GONE);
            }
        }
    }

    private void ensureEmptyView() {
        if (mEmptyView == null && mEmptyViewResId != NO_ID) {
            mEmptyView = mInflater.inflate(mEmptyViewResId, this, false);
            mEmptyView.setTag(R.id.tag_multi_state_view, TAG_EMPTY);
            addView(mEmptyView, mEmptyView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_EMPTY, mEmptyView);

            if (mViewState != VIEW_STATE_EMPTY) {
                mEmptyView.setVisibility(GONE);
            }
        }
    }

    private void ensureErrorView() {
        if (mErrorView == null && mErrorViewResId != NO_ID) {
            mErrorView = mInflater.inflate(mErrorViewResId, this, false);
            mErrorView.setTag(R.id.tag_multi_state_view, TAG_ERROR);
            addView(mErrorView, mErrorView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_ERROR, mErrorView);

            if (mViewState != VIEW_STATE_ERROR) {
                mErrorView.setVisibility(GONE);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean returnValue = false;
        MotionEvent event = MotionEvent.obtain(ev);
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }
        int eventY = (int) event.getY();
        event.offsetLocation(0, mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastY - eventY;
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    mLastY = eventY - mScrollOffset[1];
                    event.offsetLocation(0, -mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                }
                returnValue = super.onTouchEvent(event);

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0, mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                    mLastY -= mScrollOffset[1];
                }
                break;
            case MotionEvent.ACTION_DOWN:
                returnValue = super.onTouchEvent(event);
                mLastY = eventY;
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                returnValue = super.onTouchEvent(event);
                // end NestedScroll
                stopNestedScroll();
                break;
            default:
                break;
        }
        return returnValue;
    }

    // Nested Scroll implements
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        try {
            mChildHelper.setNestedScrollingEnabled(enabled);
        } catch (Exception e) {
            Log.e(TAG, "setNestedScrollingEnabled>>>" + e.getMessage());
        }
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                                        int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        View scrollView = findScrollView(this);
        if (scrollView != null) {
            return scrollView.canScrollVertically(direction);
        }
        return super.canScrollVertically(direction);
    }

    View findScrollView(ViewGroup parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            if (childView instanceof NestedScrollView || childView instanceof RecyclerView) {
                return childView;
            } else if (childView instanceof ViewGroup) {
                return findScrollView((ViewGroup) childView);
            }
        }
        return null;
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type, consumed);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
//        onNestedScrollInternal(dyUnconsumed, type, consumed);
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//        onNestedScrollInternal(dyUnconsumed, type, null);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type);
    }

    protected void onNestedScrollInternal(int dyUnconsumed, int type, @Nullable int[] consumed) {
        final int oldScrollY = getScrollY();
        scrollBy(0, dyUnconsumed);
        final int myConsumed = getScrollY() - oldScrollY;

        if (consumed != null) {
            consumed[1] += myConsumed;
        }
        final int myUnconsumed = dyUnconsumed - myConsumed;

        mChildHelper.dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null, type, consumed);
    }

    public interface StateListener {
        /**
         * Callback for when the {@link ViewState} has changed
         *
         * @param viewState The {@link ViewState} that was switched to
         */
        void onStateChanged(@ViewState int viewState);

        /**
         * Callback for when a {@link ViewState} has been inflated
         *
         * @param viewState The {@link ViewState} that was inflated
         * @param view      The {@link View} that was inflated
         */
        void onStateInflated(@ViewState int viewState, @NonNull View view);
    }
}
