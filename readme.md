# Android 多状态布局，支持布局外层下拉刷新

支持视图可以包含的四种不同状态是：内容视图，空视图，错误视图，加载中视图

## 使用多状态布局

通过XML将RatioImageView添加为布局文件，可以将其与任何其他视图一样使用

```css
<?xml version="1.0" encoding="utf-8"?>
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@id/swipe_refresh_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.fz.multistateview.MultiStateView
        android:id="@id/multi_state_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:msv_errorView="@layout/error_view"
        app:msv_emptyView="@layout/empty_view"
        app:msv_noNetworkView="@layout/no_network_view"
        app:msv_loadingView="@layout/loading_view">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:cacheColorHint="@null"
            android:scrollbars="none"/>

    </com.fz.multistateview.MultiStateView>
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

属性列表

```css
<declare-styleable name="MultiStateView">
        <attr name="msv_loadingView" format="reference" />
        <attr name="msv_emptyView" format="reference" />
        <attr name="msv_errorView" format="reference" />
        <attr name="msv_noNetworkView" format="reference" />
        <attr name="msv_processView" format="reference" />
        <attr name="msv_viewState" format="enum">
            <enum name="content" value="0" />
            <enum name="error" value="1" />
            <enum name="empty" value="2" />
            <enum name="loading" value="3" />
            <enum name="noNetwork" value="4" />
        </attr>
        <attr name="msv_animateViewChanges" format="boolean" />
    </declare-styleable>
```

```
msv_loadingView是用于loading ViewState的视图
msv_emptyView是用于empty ViewSate的视图
msv_errorView是用于error ViewState的视图
msv_noNetworkView是用于 noNetwork ViewState的视图
msv_viewState是MultiStateView的ViewState
CONTENT ViewState由XML标签内的任何内容决定。 注意**必须设置内容视图才能使视图正常工作，这是设计使然。
要切换MultiStateView的状态，只需调用即可
public void setViewState（@ViewState int state）
您还可以通过调用获取随附ViewState的视图
public View getView（@ViewState int state）
```

代码使用

```java
 MultiStatusView multiStateView = new MultiStatusView(context, contentView);注意**必须设置内容视图才能使视图正常工
 multiStateView.setEmptyViewResId(R.lauyout.empty_view);
 multiStateView.setErrorViewResId(R.lauyout.error_view);
 multiStateView.setNoNetworkViewResId(R.lauyout.no_network_view);
 multiStateView.setLoadingViewResId(R.lauyout.loading_view);
```

## 添加存储库

```py
 repositories {
        maven { url 'http://10.36.5.100:8081/repository/maven-public/' }
    }
```

## 添加依赖

```py
dependencies {
    implementation 'com.fz.multistateview:MultiStateView:1.0.1'
}
```



