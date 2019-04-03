package com.kotlinlib.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.githang.statusbar.StatusBarCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kotlinlib.other.*
import me.yokeyword.fragmentation.SupportActivity
import org.greenrobot.eventbus.EventBus

/**
 * 基类Activity
 */
abstract class AbstractKotlinActivity : Activity(), BaseInterface {

    val ACTIVITY_NAME = "ac_name"
    var startEventBus = false//是否启用EventBus
    lateinit var viewInject:LayoutId//布局ID注解，必须要有
    var colorInject:StatusBarColor?=null//状态栏颜色注解
    var orientationInject:Orientation?=null//是否是竖直方向

    //修改了地方
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        "The Current Activity is ${this.javaClass.simpleName}".logD(ACTIVITY_NAME)

        val annotations = this::class.annotations
        annotations.forEach {
            when(it.annotationClass){
                LayoutId::class->{
                    viewInject = it as LayoutId
                }
                StatusBarColor::class->{
                    colorInject = it as StatusBarColor
                }
                Orientation::class->{
                    orientationInject = it as Orientation
                }
            }
        }

        requestedOrientation = if(orientationInject==null){
            //默认是竖直方向
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }else{
            if(orientationInject?.isVertical!!)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        setContentView(viewInject.id)

        if(colorInject!=null){
            StatusBarCompat.setStatusBarColor(this, Color.parseColor(colorInject?.color))
        }

        init(savedInstanceState)

        if(startEventBus){
            EventBus.getDefault().register(this)
        }

        actList.add(this)
    }

    override fun onResume() {
        super.onResume()
        currAct = javaClass.simpleName
    }

    override fun onDestroy() {
        actList.remove(this)
        if(startEventBus){
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    protected abstract fun init(bundle: Bundle?)

    companion object {
        var gson = Gson()//所有的Activity共享一个gson
        var actList = ArrayList<Activity>()//用于储存所有的Activity实例
        var currAct:String = ""

        /**
         * 根据Activity的SimpleName来关闭它
         * @param actName String
         */
        fun finishActivityByName(actName:String){
            for(activity in actList){
                if(activity.javaClass.simpleName==actName){
                    activity.finish()
                    return
                }
            }
        }

        /**
         * 关闭所有的Activity
         */
        fun finishAllActivities(){
            for(activity in actList){
                activity.finish()
            }
        }
    }

    /**
     * 设置状态栏颜色为黑色，仅对6.0以上版本有效
     * @param isDark Boolean
     */
    fun setStatusBarTextBlack(isDark:Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val decor = window.decorView
            if(isDark){
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)//设置绘画模式
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)//设置半透明模式
                }
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }else{
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)//清除绘画模式
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)//清除半透明模式
                }
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }

    /**
     * 隐藏或显示状态栏
     * @param enable Boolean
     */
    protected fun fullscreen(enable: Boolean) {
        if (enable) { //显示状态栏
            val lp = window.attributes
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = lp
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else { //隐藏状态栏
            val lp = window.attributes
            lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            window.attributes = lp
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    /**
     * 方便在Activity中弹出Toast
     * @receiver Any
     * @param isLong Boolean
     * @param pre String
     */
    fun Any.toast(isLong: Boolean=false, gravity: Int = Gravity.BOTTOM, xOffset:Int=0, yOffset:Int=0){
        if(isLong)
            Toast.makeText(this@AbstractKotlinActivity, "${this}",
                    Toast.LENGTH_SHORT).apply {
                setGravity(gravity, xOffset, yOffset)
            }.show()
        else
            Toast.makeText(this@AbstractKotlinActivity,"${this}",
                    Toast.LENGTH_SHORT).apply {
                setGravity(gravity, xOffset, yOffset)
            }.show()
    }

    /**
     * 方便在Activity中直接使用相关单位的数字
     */
    fun Number.px2dp():Int{
        return DensityUtils.px2dip(this@AbstractKotlinActivity, this.toFloat())
    }

    fun Number.dp2px():Int{
        return DensityUtils.dip2px(this@AbstractKotlinActivity, this.toFloat())
    }

    fun Number.px2sp():Int{
        return DensityUtils.px2sp(this@AbstractKotlinActivity, this.toFloat())
    }

    fun Number.sp2px():Int{
        return DensityUtils.sp2px(this@AbstractKotlinActivity, this.toFloat())
    }

    /**
     * 设置窗口变灰
     * @param alpha Float
     */
    fun setWindowAplha(alpha:Float=0.4f){
        val attr = window.attributes
        attr.alpha = alpha
        window.attributes = attr
    }

    /**
     * 将JSON字符串转换为JSON对象数组
     * @param jsonStr String
     * @return List<T>
     */
    fun <T> strToJsonList(jsonStr:String): List<T> {
        return gson.fromJson(jsonStr, object : TypeToken<List<T>>(){}.type) as List<T>
    }

    /**
     * 渲染布局
     * @param id Int
     * @return View
     */
    fun inf(id:Int): View {
       return inflater.inflate(id, null)
    }

    /**
     * 启动Activity
     */
    inline fun <reified T: Activity> start(){
        startActivity( Intent(this, T::class.java))
    }

    /**
     * 启动Activity，可带参数
     */
    inline fun <reified T: Activity> start(vararg pairs:Pair<String,Any>){
        val intent = Intent(this, T::class.java)
        pairs.forEach {
            when(it.second){
                is String->{
                    intent.putExtra(it.first,it.second.toString())
                }
                is Int->{
                    intent.putExtra(it.first,it.second as Int)
                }
                is Boolean->{
                    intent.putExtra(it.first,it.second as Boolean)
                }
                is Double->{
                    intent.putExtra(it.first,it.second as Double)
                }
                is java.io.Serializable->intent.putExtra(it.first,it.second as java.io.Serializable)
            }
            Log.d("T_BUNDLE", ("${it.first} ${it.second}"))
        }
        startActivity(intent)
    }

}