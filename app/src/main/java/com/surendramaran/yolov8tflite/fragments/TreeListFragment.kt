package com.surendramaran.yolov8tflite.fragments

import FileUtils.Companion.generateFile
import FileUtils.Companion.goToFileIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.surendramaran.yolov8tflite.FirebaseCallback
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.SignInCallback
import com.surendramaran.yolov8tflite.TreeApplication
import com.surendramaran.yolov8tflite.adapter.TreeCardAdapter
import com.surendramaran.yolov8tflite.entities.Tree
import java.io.File

class TreeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var treeCardAdapter: TreeCardAdapter
    private lateinit var treeList: List<Tree>
    private lateinit var btnExportToCsv: Button

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val RC_SIGN_IN = 9001

    private lateinit var db: FirebaseFirestore

    private val treeViewModel: TreeViewModel by viewModels {
        TreeViewModelFactory((requireActivity().application as TreeApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tree_list, container, false)

        initializeGoogleSignIn()
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerView)
        treeCardAdapter = TreeCardAdapter(signInCallback, firebaseCallback)
        recyclerView.adapter = treeCardAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        btnExportToCsv = view.findViewById(R.id.exportButton)
        btnExportToCsv.setOnClickListener {
            exportDatabaseToCSVFile()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    val signInCallback = object : SignInCallback {
        override fun onSignIn() {
            if (mAuth.currentUser == null)
            {
                signIn()
            }
        }
    }

    val firebaseCallback = object : FirebaseCallback {
        override fun addData(treeEntity: Tree) {
            if (mAuth.currentUser != null)
            {
                progressBar.visibility = View.VISIBLE
                val tree = Tree(
                    id = treeEntity.id,
                    name = treeEntity.name,
                    latitude = treeEntity.latitude,
                    longitude = treeEntity.longitude,
                    isUploaded = true,
                    ripe = treeEntity.ripe,
                    underripe = treeEntity.underripe,
                    unripe = treeEntity.unripe,
                    flower = treeEntity.flower,
                    abnromal = treeEntity.abnromal,
                    total = treeEntity.total
                )

                db.collection("trees")
                    .document(tree.id)
                    .set(tree)
                    .addOnSuccessListener { _ ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "data uploaded", Toast.LENGTH_SHORT).show()
                        treeViewModel.update(tree)

                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Fail to upload data $e", Toast.LENGTH_SHORT).show()
                        Log.d("failed firestore", e.toString());
                    }
            }
        }
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, /*accessToken=*/ null)
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
    }

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireContext(), "treeDatabase.csv")
        if (csvFile != null) {
            exportToCSVFile(csvFile)
            val intent = goToFileIntent(requireContext(), csvFile)
            startActivity(intent)
        }
    }
    private fun exportToCSVFile(csvFile: File) {
        csvWriter().open(csvFile, append = false) {
            writeRow(listOf("id", "name", "latitude", "longitude", "ripe", "underripe", "unripe", "flower", "abnormal", "total"))
            treeList.forEachIndexed { _, tree ->
                writeRow(listOf(tree.id, tree.name, tree.latitude, tree.longitude, tree.ripe, tree.underripe, tree.unripe, tree.flower, tree.abnromal, tree.total))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        treeViewModel.allTrees.observe(viewLifecycleOwner) { trees ->
            trees?.let {
                treeList = it
                treeCardAdapter.setCardItems(it)
            }
        }
    }
}