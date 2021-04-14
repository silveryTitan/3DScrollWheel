package com.silverytitan.scrollwheel3d.view

import android.animation.ValueAnimator
import android.graphics.Rect
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class ScrollWheelLayoutManager : RecyclerView.LayoutManager() {
    /**
     * 滑动的方向
     */
    private val SCROLL_LEFT = 1//左
    private val SCROLL_RIGHT = 2//右

    private var mRecycle: Recycler? = null//RecyclerView的Item回收器
    private var mState: RecyclerView.State? = null//RecyclerView的状态器
    private val mHasAttachedItems = SparseBooleanArray()//记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val mItemRects = SparseArray<Rect>()
    private var mItemWidth = 0
    private var mItemHeight = 0
    private var mIntervalWidth = 0
    private var mStartX = 0
    private var mTotalWidth = 0
    private var mSumDx = 0
    private var child: View? = null
    private var moveX = 0
    private var isEnd = false

    /**
     * 计算Item缩放系数
     *
     * @param x Item的偏移量
     * @return 缩放系数
     */
    private val s = 0.8f
    private val n = 0.1f
    private val s2 = s + n //0.94
    private val M_MAX_ROTATION_Y = 15.0f//最大Y轴旋转度数
    private var mTravel = 0

    fun getCenterPosition(): Int {
        var pos = (mSumDx / getIntervalWidth())
        if ((mSumDx % getIntervalWidth()) > getIntervalWidth() * 0.5f) pos++
        return pos
    }

    public fun getFirstVisiblePosition(): Int {
        if (childCount <= 0) {
            return 0
        }
        val view = getChildAt(0)
        return getPosition(view!!)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler?.let {
            if (itemCount == 0) {//没有Item，界面空置
                detachAndScrapAttachedViews(it)
            } else {
                mRecycle = it
                mState = state
                mHasAttachedItems.clear()
                mItemRects.clear()
                detachAndScrapAttachedViews(it)
                val childView = it.getViewForPosition(0)
                measureChildWithMargins(childView, 0, 0)
                mItemWidth = getDecoratedMeasuredWidth(childView)
                mItemHeight = getDecoratedMeasuredHeight(childView)
                mIntervalWidth = getIntervalWidth()
                mStartX = width / 2 - mItemWidth / 2

                var offsetX = 0//定义水平方向的偏移量
                for (i in 0 until itemCount) {
                    val rect = Rect(
                        mStartX + offsetX,
                        mItemWidth / 2,
                        mStartX + offsetX + mItemWidth,
                        mItemHeight
                    )
                    mItemRects.put(i, rect)
                    mHasAttachedItems.put(i, false)
                    offsetX += mIntervalWidth
                }
                layoutItems(it, SCROLL_RIGHT)
                //如果所有子View的宽度和没有填满RecyclerView的宽度，
                // 则将宽度设置为RecyclerView的宽度
                mTotalWidth = max(offsetX, getHorizontalSpace())
            }
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: Recycler?,
        state: RecyclerView.State?
    ): Int {
        if (childCount <= 0) {
            return dx
        }
        var travel = dx
        //如果滑动到最顶部
        //如果滑动到最顶部
        if (mSumDx + dx < 0) {
            travel = -mSumDx
        } else if (mSumDx + dx > getMaxOffset()) {
            //如果滑动到最底部
            travel = getMaxOffset() - mSumDx
        }
        mTravel = travel
        mSumDx += travel
        layoutItems(recycler!!, SCROLL_RIGHT)
        return travel
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val finalOffset: Int = calculateOffsetForPosition(position)
        if (mRecycle != null || mState != null) { //如果RecyclerView还没初始化完，先记录下要滚动的位置
            startScroll(mSumDx, finalOffset)
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                //滚动停止时
                isEnd = true
                handleChildView(child!!, moveX)
                requestLayout()
            }
            RecyclerView.SCROLL_STATE_DRAGGING -> {
            }//拖拽滚动时
            RecyclerView.SCROLL_STATE_SETTLING -> {
            }//动画滚动时
        }
    }

    /**
     * 滚动到指定X轴位置
     *
     * @param from X轴方向起始点的偏移量
     * @param to   X轴方向终点的偏移量
     */
    private fun startScroll(from: Int, to: Int) {
        val mAnimation = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
        if (mAnimation != null && mAnimation.isRunning) {
            mAnimation.cancel()
        }
        val direction = if (from < to) SCROLL_RIGHT else SCROLL_LEFT
        mAnimation.duration = 200
        mAnimation.interpolator = DecelerateInterpolator()
        mAnimation.addUpdateListener { anim ->
            mSumDx = (anim.animatedValue as Float).roundToInt()
            layoutItems(mRecycle!!, direction)
        }
        mAnimation.start()
    }

    private fun layoutItems(recycler: Recycler, scrollDirection: Int) {
        val visibleRect = getVisibleArea()
        //回收越界子View
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            val rect = mItemRects[position]
            if (!Rect.intersects(rect, visibleRect)) {
                removeAndRecycleView(child, recycler)
                mHasAttachedItems.put(position, false)
            } else {
                layoutDecoratedWithMargins(
                    child, rect.left - mSumDx,
                    rect.top, rect.right - mSumDx, rect.bottom
                )
                handleChildView(child, rect.left - mStartX - mSumDx)
                mHasAttachedItems.put(position, true)
            }
        }
        val min = 0
        val max = itemCount
        for (i in min until max) {
            val rect = mItemRects[i]
            if (Rect.intersects(visibleRect, rect) && !mHasAttachedItems[i]) {
                val child = recycler.getViewForPosition(i)
                if (scrollDirection == SCROLL_RIGHT) addView(child, 0)
                else addView(child)
                measureChildWithMargins(child, 0, 0)
                layoutDecoratedWithMargins(
                    child,
                    rect.left - mSumDx,
                    rect.top,
                    rect.right - mSumDx,
                    rect.bottom
                )
                handleChildView(child, rect.left - mStartX - mSumDx)
                mHasAttachedItems.put(i, true)
            }
        }
    }

    private fun getHorizontalSpace(): Int {
        return width - paddingLeft - paddingRight
    }

    private fun handleChildView(child: View, moveX: Int) {
        this.child = child
        this.moveX = moveX
        val radio: Float = computeScale(moveX, getPosition(child))
        val rotation: Float = computeRotationY(moveX)

        child.pivotY = (child.height / 4).toFloat()

        child.scaleX = radio
        child.scaleY = radio

        if (isEnd && getPosition(child) == getCenterPosition()) {
            child.rotationY = 0f
        } else {
            child.rotationY = rotation
        }
    }

    /**
     * 获取最大偏移量
     */
    private fun getMaxOffset(): Int {
        return (itemCount - 1) * getIntervalWidth()
    }

    /**
     * 计算Item所在的位置偏移
     *
     * @param position 要计算Item位置
     */
    private fun calculateOffsetForPosition(position: Int): Int {
        return (getIntervalWidth() * position).toFloat().roundToInt()
    }

    private fun computeScale(x: Int, position: Int): Float {
        val b = BigDecimal((abs(x * 2.0f / (11f * getIntervalWidth())) + s).toInt())
        var scale = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).toFloat()
        if (scale < s2) scale = s2
        return scale
    }

    private fun getVisibleArea(): Rect {
        return Rect(
            paddingLeft + mSumDx,
            paddingTop,
            width - paddingRight + mSumDx,
            height - paddingBottom
        )
    }

    private fun computeRotationY(x: Int): Float {
        var rotationY: Float
        rotationY = -M_MAX_ROTATION_Y * x / getIntervalWidth()
        if (abs(rotationY) > M_MAX_ROTATION_Y) {
            rotationY = if (rotationY > 0) M_MAX_ROTATION_Y
            else -M_MAX_ROTATION_Y
        }
        return rotationY
    }

    private fun getIntervalWidth(): Int {
        return mItemWidth + mItemWidth / 5
    }
}