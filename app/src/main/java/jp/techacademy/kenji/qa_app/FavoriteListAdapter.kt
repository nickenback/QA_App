package jp.techacademy.kenji.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import kotlinx.android.synthetic.main.list_questions.view.*

class FavoriteListAdapter(context: Context): BaseAdapter() {

    private var mLayoutInflater: LayoutInflater
    private var mFavoriteArrayList = ArrayList<Question>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mFavoriteArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mFavoriteArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_questions, parent, false)
        }

        val titleText = convertView!!.titleTextView as TextView
        titleText.text = mFavoriteArrayList[position].title

        val nameText = convertView.nameTextView as TextView
        nameText.text = mFavoriteArrayList[position].name

        val resText = convertView.resTextView as TextView
        val resNum = mFavoriteArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = mFavoriteArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.imageView as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setFavoriteArrayList(favoriteArrayList: ArrayList<Question>) {
        mFavoriteArrayList = favoriteArrayList
    }
}