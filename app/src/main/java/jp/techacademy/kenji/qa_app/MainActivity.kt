package jp.techacademy.kenji.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64

import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar  // ← 追加
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
// findViewById()を呼び出さずに該当Viewを取得するために必要となるインポート宣言
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    private  var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var mFavoriteRef: DatabaseReference? = null
    private lateinit var mQuestionRef: DatabaseReference
    private lateinit var mFavoriteArrayList: ArrayList<Question>
//    private lateinit var mFavoriteAdapter: FavoriteListAdapter

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
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

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

//    private val mFavoriteListener = object : ChildEventListener {
//        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
//            val map = dataSnapshot.value as Map<String, String>
//            val QuestionID = dataSnapshot.key
//            val Genre = map["genre"] ?: ""
//
//
//            mQuestionRef = mDatabaseReference.child(ContentsPATH).child(Genre).child(QuestionID.toString())
//            mQuestionRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val data = snapshot.value as Map<String, String>
//                        val title = data["title"] ?: ""
//                        val body = data["body"] ?: ""
//                        val name = data["name"] ?: ""
//                        val uid = data["uid"] ?: ""
//                        val imageString = data["image"] ?: ""
//                        val bytes =
//                            if (imageString.isNotEmpty()) {
//                                Base64.decode(imageString, Base64.DEFAULT)
//                            } else {
//                                byteArrayOf()
//                            }
//                        val answerArrayList = ArrayList<Answer>()
//                        val answerMap = map["answers"] as Map<String, String>?
//                        if (answerMap != null) {
//                            for (key in answerMap.keys) {
//                                val temp = answerMap[key] as Map<String, String>
//                                val answerBody = temp["body"] ?: ""
//                                val answerName = temp["name"] ?: ""
//                                val answerUid = temp["uid"] ?: ""
//                                val answer = Answer(answerBody, answerName, answerUid, key)
//                                answerArrayList.add(answer)
//                            }
//                        }
//                        val favorite = Question(title, body, name, uid, QuestionID.toString(),
//                            Genre.toInt(), bytes, answerArrayList)
//                        mFavoriteArrayList.add(favorite)
////                        mFavoriteAdapter.notifyDataSetChanged()
//
//                    }
//
//                    override fun onCancelled(firebaseError: DatabaseError) {}
//                })
//
//
//        }
//
//        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
//            val map = dataSnapshot.value as Map<String, String>
//
//        }
//
//        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//
//
//        }
//
//        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
//
//        }
//
//        override fun onCancelled(databaseError: DatabaseError) {
//
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        setSupportActionBar(toolbar)

        // fabにClickリスナーを登録
        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(
                    view,
                    getString(R.string.question_no_select_genre),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {

            }
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

//        val navigationView = findViewById<NavigationView>(R.id.nav_view)
//        navigationView.setNavigationItemSelectedListener(this)

        nav_view.setNavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

//        mFavoriteAdapter = FavoriteListAdapter(this, _)
//        mFavoriteArrayList = ArrayList<Question>()
//        mFavoriteAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            nav_view.menu.findItem(R.id.nav_favorite).isVisible = false
        } else {
            nav_view.menu.findItem(R.id.nav_favorite).isVisible = true
        }

//        val user = FirebaseAuth.getInstance().currentUser
//        mFavoriteArrayList.clear()
//        if (user == null) {
//        } else {
//
//            mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
//            mFavoriteRef!!.addChildEventListener(mFavoriteListener)
//        }



        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする

        if(mGenre == 0) {
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }
    }

//    override fun onPause(){
//        super.onPause()
//        mFavoriteArrayList.clear()
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)



        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        }else if (id == R.id.nav_favorite) {

            val intent = Intent(applicationContext, FavoriteList::class.java)
//            intent.putExtra("favorite", mFavoriteArrayList)
//            mFavoriteRef!!.removeEventListener(mFavoriteListener)

            startActivity(intent)
//            mFavoriteArrayList.clear()

        }

        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

//        mFavoriteArrayList.clear()

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)


//        val user = FirebaseAuth.getInstance().currentUser
//
//        if (user == null) {
//        } else {
//
//            mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
//            mFavoriteRef!!.addChildEventListener(mFavoriteListener)
//        }


        return true
    }


}