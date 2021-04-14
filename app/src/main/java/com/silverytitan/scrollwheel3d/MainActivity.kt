package com.silverytitan.scrollwheel3d

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.silverytitan.scrollwheel3d.databinding.ItemMenuBinding
import com.silverytitan.scrollwheel3d.view.ScrollWheelLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val menuList = mutableListOf(
        R.mipmap.icon_menu_people,
        R.mipmap.icon_menu_equipment,
        R.mipmap.icon_menu_qc,
        R.mipmap.icon_menu_dynamic,
        R.mipmap.icon_menu_study,
        R.mipmap.icon_menu_fly,
        R.mipmap.icon_menu_bird,
        R.mipmap.icon_menu_fire,
        R.mipmap.icon_menu_fly_stop,
        R.mipmap.icon_menu_light,
        R.mipmap.icon_menu_breed
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_menu.layoutManager = ScrollWheelLayoutManager()
        rv_menu.adapter = MenuAdapter(menuList)
        LinearSnapHelper().attachToRecyclerView(rv_menu)
    }

    inner class MenuAdapter(data: MutableList<Int>) :
        BaseQuickAdapter<Int, BaseDataBindingHolder<ItemMenuBinding>>(R.layout.item_menu, data) {
        override fun convert(holder: BaseDataBindingHolder<ItemMenuBinding>, item: Int) {
            val bind = holder.dataBinding
            bind!!.ivRoot.setImageBitmap(compoundBitmap(item))
//            val view = holder.getView<RotationImageView>(R.id.iv_root)
//            view.setImageBitmap(compoundBitmap(item))
//            holder.setImageBitmap(R.id.iv_root, ) //合成图片
            holder.itemView.setOnClickListener {

            }
        }

        private fun compoundBitmap(item: Int): Bitmap? {
            val originalBitmap = BitmapFactory.decodeResource(resources, item) //把资源图片变成一个Bitmap对象
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
}