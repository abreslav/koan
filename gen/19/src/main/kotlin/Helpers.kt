package kotlinx.android.koan

import android.content.Context
import android.view.ViewGroup
import android.view.ViewManager
import android.view.View
import android.app.Activity
import android.app.Fragment
import java.util.HashMap

private data class ViewProps(var listeners: HashMap<String, ListenerHelper>, var style: (Any) -> Unit, var realTag: Any? = null)
private val defaultStyle: (Any) -> Unit = {}

private trait ListenerHelper {
  fun apply()
}

public fun View.style(style: (Any) -> Unit): Unit = applyStyle(this, style)

public var View.tag: Any?
  get() = {
    val tag = getTag()
    val props = tag as? ViewProps
    if (props!=null) props.realTag else tag
  }
  set(tag) {
    var props = getTag() as? ViewProps
    if (props!=null) {
      props!!.realTag = tag
    } else setTag(tag)
  }

public fun <T: View> __dslAddView(view: (ctx: Context) -> T, init: T.() -> Unit, manager: ViewManager): T {
  return addView(view(manager.dslContext), init, manager)
}

public fun <T: View> __dslAddView(view: (ctx: Context) -> T, init: T.() -> Unit, act: Activity): T {
  return act.addActivityTopLevelView(view(act), init)
}

public fun <T: View> __dslAddView(view: (ctx: Context) -> T, init: T.() -> Unit, ctx: Context): T {
  return ctx.addContextTopLevelView(view(ctx), init)
}

public fun <T: View> __dslAddView(view: (ctx: Context) -> T, init: T.() -> Unit, fragment: Fragment): T {
  val ctx = fragment.getActivity()!!
  return fragment.addFragmentTopLevelView(view(ctx), init)
}

private fun <T: View> addView(v: T, init: T.() -> Unit, manager: ViewManager): T {
  v.setTag(ViewProps(hashMapOf(), defaultStyle, v.getTag()))
  v.init()
  val props = v.getTag() as? ViewProps
  if (props != null) {
    props.listeners.values().forEach { it.apply() }
    v.setTag(props.realTag)
  }
  when (manager) {
    is ViewGroup -> manager.addView(v)
    is UiHelper -> manager.addView(v)
    else -> throw RuntimeException("${manager.toString()} is the wrong parent")
  }
  return v
}

private fun <T: View> Activity.addActivityTopLevelView(v: T, init: T.() -> Unit): T {
  UI { addView(v, init, this) }
  return v
}

private fun <T: View> Fragment.addFragmentTopLevelView(v: T, init: T.() -> Unit): T {
  UI { addView(v, init, this) }
  return v
}

private fun <T: View> Context.addContextTopLevelView(v: T, init: T.() -> Unit): T {
  UI { addView(v, init, this) }
  return v
}

private fun applyStyle(v: View, style: (Any) -> Unit) {
  style(v)
  if (v is ViewGroup) {
    val maxId = v.getChildCount()-1
    for (i in 0..maxId) {
      val maybeChild = v.getChildAt(i)
      if (maybeChild!=null) applyStyle(maybeChild, style)
    }
  }
}

private val ViewManager.dslContext: Context
  get() {
    return when(this) {
      is ViewGroup -> this.getContext()!!
      is UiHelper -> this.ctx
      else -> throw RuntimeException("${this.toString()} is the wrong parent")
    }
  }

class UiHelper(val ctx: Context, private val setContentView: Boolean = true): ViewManager {
  private var view: View? = null

  fun toView() = view!!
  override fun addView(view: View, params: ViewGroup.LayoutParams) {
    addView(view)
  }

  fun setContentView(): Unit = when (ctx) {
    is Activity -> ctx.setContentView(view)
    else -> {}
  }

  fun addView(view: View) {
    this.view = view
    if (setContentView) {
      when (ctx) {
        is Activity -> ctx.setContentView(view)
        else -> {}
      }
    }
  }
  override fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
    throw UnsupportedOperationException()
  }
  override fun removeView(view: View) {
    throw UnsupportedOperationException()
  }
}

public fun Context.UI(setContentView: Boolean, init: UiHelper.() -> Unit): UiHelper {
  val dsl = UiHelper(this, setContentView)
  dsl.init()
  return dsl
}

public fun Context.UI(init: UiHelper.() -> Unit): UiHelper {
  val dsl = UiHelper(this, false)
  dsl.init()
  return dsl
}

public fun Activity.UI(init: UiHelper.() -> Unit): UiHelper = UI(true, init)

public fun Fragment.UI(init: UiHelper.() -> Unit): UiHelper = getActivity()!!.UI(false, init)

