package com.silverytitan.scrollwheel3d.adapter

import android.graphics.*
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ResourceUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.silverytitan.scrollwheel3d.R

class MenuAdapter(data: MutableList<Int>) :
    BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_menu, data) {
    override fun convert(holder: BaseViewHolder, item: Int) {
        holder.setImageBitmap(R.id.iv_root, compoundBitmap(item))
        holder.itemView.setOnClickListener {

        }
    }

    private fun compoundBitmap(item: Int): Bitmap? {
        val originalBitmap =
            ConvertUtils.drawable2Bitmap(ResourceUtils.getDrawable(item)) //把资源图片变成一个Bitmap对象
        //生成下面的一半图片
        val matrix = Matrix()
        matrix.setScale(1f, -1f) //翻转
        val invertBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, false
        )
        //创建一个空的位图
        val compoundBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height + invertBitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(compoundBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        canvas.drawBitmap(
            invertBitmap, 0f, (originalBitmap.height - 2).toFloat(), null
        ) //上下俩个图片的间距
        val paint = Paint()
        // 设置渐变颜色
        val shader = LinearGradient(
            0f,
            (originalBitmap.height).toFloat(),
            0f,
            compoundBitmap.height.toFloat(),
            0x70ffffff,
            0x00ffffff,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(
            0f,
            //            (originalBitmap.height + 5).toFloat(),
            (originalBitmap.height).toFloat(),
            originalBitmap.width.toFloat(),
            compoundBitmap.height.toFloat(),
            paint
        )
        return compoundBitmap
    }
}