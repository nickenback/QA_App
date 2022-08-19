package jp.techacademy.kenji.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite_list.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.listView

class FavoriteList : AppCompatActivity() {

//    private lateinit var mFavorite: Question
//    private var mFavoriteList = ArrayList<Question>()
    private lateinit var mFavoriteAdapter: FavoriteListAdapter

    private var mFavoriteRef: DatabaseReference? = null
    private lateinit var mQuestionRef: DatabaseReference

    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var mFavoriteArrayList: ArrayList<Question>

    private val mFavoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val QuestionID = dataSnapshot.key
            val Genre = map["genre"] ?: ""


            mQuestionRef = mDatabaseReference.child(ContentsPATH).child(Genre).child(QuestionID.toString())
            mQuestionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<String, String>
                    val title = data["title"] ?: ""
                    val body = data["body"] ?: ""
                    val name = data["name"] ?: ""
                    val uid = data["uid"] ?: ""
                    val imageString = data["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }
                    val favorite = Question(title, body, name, uid, QuestionID.toString(),
                        Genre.toInt(), bytes, answerArrayList)
                    mFavoriteArrayList.add(favorite)
                    mFavoriteAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            })






        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {


        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_list)



        mDatabaseReference = FirebaseDatabase.getInstance().reference

        val user = FirebaseAuth.getInstance().currentUser
//        mFavoriteArrayList.clear()
        if (user == null) {
        } else {

            mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
            mFavoriteRef!!.addChildEventListener(mFavoriteListener)
        }

//        mFavoriteList = intent.getSerializableExtra("favorite") as ArrayList<Question>
//        var mFavoriteList = mFavoriteArrayList

        mFavoriteAdapter = FavoriteListAdapter(this)
        mFavoriteArrayList = ArrayList<Question>()
        mFavoriteAdapter.notifyDataSetChanged()

        mFavoriteArrayList.clear()
        mFavoriteAdapter.setFavoriteArrayList(mFavoriteArrayList)
        FavoriteListView.adapter = mFavoriteAdapter

        mFavoriteRef!!.removeEventListener(mFavoriteListener)



        FavoriteListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mFavoriteArrayList[position])
            startActivity(intent)
//            mFavoriteRef!!.removeEventListener(mFavoriteListener)
        }
    }
//    override fun onPause(){
//        super.onPause()
//
//        mFavoriteArrayList.clear()
//    }

    override fun onStart(){
        super.onStart()
//        mFavoriteAdapter = FavoriteListAdapter(this)
//
//        mFavoriteAdapter.notifyDataSetChanged()
//        mFavoriteAdapter.setFavoriteArrayList(mFavoriteArrayList)
//        FavoriteListView.adapter = mFavoriteAdapter

//        FavoriteListView.setOnItemClickListener { parent, view, position, id ->
//        // Questionのインスタンスを渡して質問詳細画面を起動する
//            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
//            intent.putExtra("question", mFavoriteArrayList[position])
//            startActivity(intent)
//            mFavoriteRef!!.removeEventListener(mFavoriteListener)
//
//        }
    }

    override fun onResume(){
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        mFavoriteArrayList.clear()
        if (user == null) {
        } else {

            mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
            mFavoriteRef!!.addChildEventListener(mFavoriteListener)
        }
    }

    override fun onPause(){
        super.onPause()
        mFavoriteRef!!.removeEventListener(mFavoriteListener)
    }
}
