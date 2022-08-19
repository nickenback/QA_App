package jp.techacademy.kenji.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*


class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private lateinit var mFavoriteRef1: DatabaseReference


    private var isFavorite: Boolean? = null


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val mFavoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            isFavorite = true
            update()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            isFavorite = false
            update()

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title


        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        var dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


//        var user = FirebaseAuth.getInstance().currentUser
//        if (user == null) {
//
//        } else {
//            mFavoriteRef = dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
//            mFavoriteRef.addChildEventListener(mFavoriteListener)
//
//        }





        favorite.setOnClickListener {

            var user = FirebaseAuth.getInstance().currentUser
            var dataBaseReference = FirebaseDatabase.getInstance().reference
            mFavoriteRef1 = dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid).child(GenrePATH)

            if(isFavorite == true){
                mFavoriteRef1.setValue(null)
//                isFavorite = false

            }else{

                mFavoriteRef1.setValue(mQuestion.genre.toString())


            }
            update()
        }

//        update()

    }

    private fun update() {

        val user = FirebaseAuth.getInstance().currentUser
//        if (user == null) {
//
//        } else {
//
//            var dataBaseReference = FirebaseDatabase.getInstance().reference
//            mFavoriteRef =
//                dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
//            mFavoriteRef.addChildEventListener(mFavoriteListener)
//        }

        if (user == null) {
            favorite.hide()
        }else {
            favorite.setImageResource(if (isFavorite == true) R.drawable.ic_star else R.drawable.ic_star_border)
            favorite.show()
        }
//        mFavoriteRef.removeEventListener((mFavoriteListener))

    }

    override fun onStart(){
        super.onStart()
        var user = FirebaseAuth.getInstance().currentUser
        var dataBaseReference = FirebaseDatabase.getInstance().reference
        if (user == null) {

        } else {
            mFavoriteRef = dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            mFavoriteRef.addChildEventListener(mFavoriteListener)

        }
        update()


    }

    override fun onResume(){
        super.onResume()

//        update()
    }

    override fun onStop(){
        super.onStop()
        mFavoriteRef.removeEventListener((mFavoriteListener))
        update()

    }

}