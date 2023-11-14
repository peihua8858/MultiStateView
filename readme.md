:heartpulse:MultiStateView:heartpulse:
一款针对Android平台下的多状态布局，支持布局外层下拉刷新

支持视图可以包含的四种不同状态是：内容视图，空视图，错误视图，加载中视图


[![Jitpack](https://jitpack.io/v/peihua8858/MultiStateView.svg)](https://github.com/peihua8858)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg)](https://github.com/peihua8858)
[![Star](https://img.shields.io/github/stars/peihua8858/MultiStateView.svg)](https://github.com/peihua8858/MultiStateView)

## 目录
-[最新版本](https://github.com/peihua8858/MultiStateView/releases/tag/1.0.6)<br>
-[如何引用](#如何引用)<br>
-[进阶使用](#进阶使用)<br>
-[如何提Issues](https://github.com/peihua8858/MultiStateView/wiki/%E5%A6%82%E4%BD%95%E6%8F%90Issues%3F)<br>
-[License](#License)<br>

## 如何引用
* 把 `maven { url 'https://jitpack.io' }` 加入到 repositories 中
* 添加如下依赖，末尾的「latestVersion」指的是MultiStateView [![Download](https://jitpack.io/v/peihua8858/MultiStateView.svg)](https://jitpack.io/#peihua8858/MultiStateView) 里的版本名称，请自行替换。
使用Gradle
```sh
repositories {
  google()
  maven { url 'https://jitpack.io' }
}

dependencies {
  // PictureSelector
  implementation 'com.github.peihua8858:MultiStateView:${latestVersion}'
}
```

或者Maven:

```xml
<dependency>
  <groupId>com.github.peihua8858</groupId>
  <artifactId>MultiStateView</artifactId>
  <version>${latestVersion}</version>
</dependency>
```
## 进阶使用

通过XML将RatioImageView添加为布局文件，可以将其与任何其他视图一样使用

```xml
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

```xml
<declare-styleable name="MultiStateView">
        <attr name="msv_loadingView" format="reference" />
        <attr name="msv_emptyView" format="reference" />
        <attr name="msv_errorView" format="reference" />
        <attr name="msv_noNetworkView" format="reference" />
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
属性名 | 说明 | 默认值
:----------- | :----------- | :-----------
msv_viewState         | 视图默认状态        | 0
msv_loadingView         | loading状态视图        | -1
msv_emptyView         | empty状态视图        | -1
msv_errorView         | error状态视图        | -1
msv_noNetworkView         | noNetwork状态视图        | -1
msv_animateViewChanges         | 状态改变时是否执行动画        | false

##### 注意
```
1、CONTENT ViewState由XML标签内的任何内容决定。 注意**必须设置内容视图才能使视图正常工作，这是设计使然。
2、要切换MultiStateView的状态，只需调用即可public void setViewState（@ViewState int state）
3、您还可以通过调用获取随附ViewState的视图public View getView（@ViewState int state）
```

代码使用

```java
 MultiStatusView multiStateView = new MultiStatusView(context, contentView);注意**必须设置内容视图才能使视图正常工
 multiStateView.setEmptyViewResId(R.lauyout.empty_view);
 multiStateView.setErrorViewResId(R.lauyout.error_view);
 multiStateView.setNoNetworkViewResId(R.lauyout.no_network_view);
 multiStateView.setLoadingViewResId(R.lauyout.loading_view);
```

## License
```sh
Copyright 2023 peihua

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



