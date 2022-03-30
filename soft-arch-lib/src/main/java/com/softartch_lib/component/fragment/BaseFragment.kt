package com.softartch_lib.component.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.softartch_lib.R
import com.softartch_lib.component.dialogs.CustomeProgressDialog
import kotlinx.android.synthetic.main.fragment_base.*
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.place_holder_layout.*
import org.koin.standalone.KoinComponent

abstract class BaseFragment : Fragment() {

    @LayoutRes
    abstract fun layoutResource(): Int

    protected lateinit var contentView: View
    lateinit var progressDialog: CustomeProgressDialog
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_base, container, false)

    override fun onViewCreated(parentView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(parentView, savedInstanceState)
        setHasOptionsMenu(true)
        progressDialog= CustomeProgressDialog(requireActivity())
        stub.apply {
            layoutResource = layoutResource()
            setOnInflateListener { _, childView ->
                contentView = childView
                onViewInflated(parentView, childView) }

            inflate()

        }

    }

    open fun onViewInflated(parentView: View, childView: View) {
    }


    protected fun hideProgress() {
        if (loadingView!=null) {
            loadingView.visibility = View.GONE
        }else{
            println("null pointer when hide progress")
        }
    }

    fun hideError(){
        placeHolderLayout.visibility = View.GONE
    }

    protected fun showProgress() {
        loadingView.visibility = View.VISIBLE
        hideError()
    }

    protected fun showError(error: String) {
        if (  placeHolderLayout!=null){
            placeHolderLayout.visibility = View.VISIBLE
            tvPlaceHolderMessage.text = error
        }
    }


    fun showProgressDialog(){
        progressDialog.show()
    }

    fun hideProgressDialog(){
        progressDialog.dismiss()
    }


    fun setPlaceHolderResourc(@DrawableRes placeHolderResourc:Int){
        imgViewPlaceHolder.visibility=View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgViewPlaceHolder.setImageDrawable(activity!!.getDrawable(placeHolderResourc))
        }else{
            imgViewPlaceHolder.setImageDrawable(activity!!.resources.getDrawable(placeHolderResourc))
        }
    }

}