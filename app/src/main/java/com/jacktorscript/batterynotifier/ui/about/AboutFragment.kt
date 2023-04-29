package com.jacktorscript.batterynotifier.ui.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.jacktorscript.batterynotifier.R
import com.jacktorscript.batterynotifier.databinding.FragmentAboutBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException


class AboutFragment : Fragment(), RecyclerViewClickListener {

    private var _binding: FragmentAboutBinding? = null
    private lateinit var dataModelArrayList: ArrayList<DataModel>
    private var recyclerView: RecyclerView? = null
    private var dataInfo: TextView? = null
    private var networkStatus: TextView? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.team_recycler)
        networkStatus = view.findViewById(R.id.network_status)
        dataInfo = view.findViewById(R.id.data_info)

        fetchingTeamJSON(view)

        //App version textView
        val appVersion = view.findViewById(R.id.app_version) as TextView
        appVersion.text = getAppVersion(requireContext())

        //View all team Button
        val otherTeamButton = view.findViewById(R.id.otherTeam_btn) as Button
        otherTeamButton.setOnClickListener {
            fetchingOtherTeamJSON()
        }

        //Changelog LinearLayout
        val changelogButton = view.findViewById(R.id.changelog_btn) as LinearLayout
        changelogButton.setOnClickListener {
            changelog()
        }

        //Author LinearLayout
        val authorButton = view.findViewById(R.id.author_btn) as LinearLayout
        authorButton.setOnClickListener {
            visitProfileAuthor()
        }
    }


    // Mendapatkan versi aplikasi
    @Suppress("DEPRECATION")
    private fun getAppVersion(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }


    //Team - In Project
    private fun fetchingTeamJSON(view: View) {
        val progressBar = view.findViewById<LinearProgressIndicator>(R.id.progressBar)
        // Request a string response from the provided URL
        val url =
            "https://jacktor.com/apps/team-json.php?package=" + requireContext().packageName +
                    "&status=1&v=" + getAppVersion(requireContext())
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                //Log.d("TEAM", ">>$response")

                if (isAdded && isVisible) {
                    networkStatus?.text = getString(R.string.online)
                    dataInfo?.text = getString(R.string.data_received_from_jacktor_com)
                }

                val obj = JSONObject(response)
                if (obj.optString("status") == "true") {
                    progressBar?.visibility = View.GONE

                    dataModelArrayList = ArrayList()
                    val dataArray = obj.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {

                        val teamModel = DataModel()
                        val dataObj = dataArray.getJSONObject(i)

                        teamModel.setNames(dataObj.getString("name"))
                        teamModel.setUsernames(dataObj.getString("username"))
                        teamModel.setStatus(dataObj.getString("projectStatus"))
                        teamModel.setimgURLs(dataObj.getString("imgURL"))

                        dataModelArrayList.add(teamModel)

                    }

                    if (isAdded) {
                        recyclerView!!.adapter =
                            AdapterTeam(requireContext(), dataModelArrayList, this)
                        recyclerView!!.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                    }

                }


            })
        { error ->
            Log.d("TEAM", ">>$error")
            networkStatus?.text = getString(R.string.offline)
            dataInfo?.text = getString(R.string.data_provided_by_app)


            //Offline atau gagal
            val obj = JSONObject(fetchingLocalJson(requireContext(), "team.json")!!)
            dataModelArrayList = ArrayList()
            val dataArray = obj.getJSONArray("data")

            for (i in 0 until dataArray.length()) {

                val teamModel = DataModel()
                val dataObj = dataArray.getJSONObject(i)

                teamModel.setNames(dataObj.getString("name"))
                teamModel.setUsernames(dataObj.getString("username"))
                teamModel.setStatus(dataObj.getString("projectStatus"))
                teamModel.setimgURLs(dataObj.getString("imgURL"))

                dataModelArrayList.add(teamModel)

            }

            if (isAdded) {
                recyclerView!!.adapter =
                    AdapterTeam(requireContext(), dataModelArrayList, this)
                recyclerView!!.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }

            progressBar?.visibility = View.GONE

        }
        // Add the request to the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        queue.add(stringRequest)

    }


    //Team - Non Project
    private fun fetchingOtherTeamJSON() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val inflater = layoutInflater
        val customView = inflater.inflate(R.layout.other_team_dialog, null)
        val recyclerViewOther = customView.findViewById<RecyclerView>(R.id.other_team_recycler)
        val progressBar = customView.findViewById<LinearProgressIndicator>(R.id.progressBar)

        // Request a string response from the provided URL
        val url =
            "https://jacktor.com/apps/team-json.php?package=" + requireContext().packageName +
                    "&status=0&v=" + getAppVersion(requireContext())
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                //Log.d("TEAM", ">>$response")
                //networkStatus?.text = getString(R.string.online)

                val obj = JSONObject(response)
                if (obj.optString("status") == "true") {

                    progressBar?.visibility = View.GONE

                    dataModelArrayList = ArrayList()
                    val dataArray = obj.getJSONArray("data")

                    for (i in 0 until dataArray.length()) {

                        val teamModel = DataModel()
                        val dataObj = dataArray.getJSONObject(i)

                        teamModel.setNames(dataObj.getString("name"))
                        teamModel.setUsernames(dataObj.getString("username"))
                        teamModel.setStatus(dataObj.getString("projectStatus"))
                        teamModel.setimgURLs(dataObj.getString("imgURL"))

                        dataModelArrayList.add(teamModel)

                    }

                    if (isAdded) {
                        recyclerViewOther!!.adapter =
                            AdapterTeam(requireContext(), dataModelArrayList, this)
                        recyclerViewOther.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                    }


                }
            })
        { error ->
            Log.d("TEAM", ">>$error")
            networkStatus?.text = getString(R.string.offline)
            dataInfo?.text = getString(R.string.data_provided_by_app)


            //Offline atau gagal
            val obj = JSONObject(fetchingLocalJson(requireContext(), "other-team.json")!!)
            dataModelArrayList = ArrayList()
            val dataArray = obj.getJSONArray("data")

            for (i in 0 until dataArray.length()) {

                val teamModel = DataModel()
                val dataObj = dataArray.getJSONObject(i)

                teamModel.setNames(dataObj.getString("name"))
                teamModel.setUsernames(dataObj.getString("username"))
                teamModel.setStatus(dataObj.getString("projectStatus"))
                teamModel.setimgURLs(dataObj.getString("imgURL"))

                dataModelArrayList.add(teamModel)

            }

            if (isAdded) {
                recyclerViewOther!!.adapter =
                    AdapterTeam(requireContext(), dataModelArrayList, this)
                recyclerViewOther.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }

            progressBar?.visibility = View.GONE
        }
        // Add the request to the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        queue.add(stringRequest)

        dialog.setView(customView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { dialogBtn, _ ->
                // Respond to neutral button press
                dialogBtn.cancel()
            }
            .show()
    }


    private fun fetchingLocalJson(context: Context, filename: String): String? {
        val json: String
        try {
            val inputStream = context.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.use { it.read(buffer) }
            json = String(buffer)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return json
    }


    //Visit Profile
    override fun onItemClicked(data: DataModel) {
        val openURL = Intent(Intent.ACTION_VIEW)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.visit_this_profile_title))
            .setMessage(data.name + " (" + data.username + ")")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                // Respond to neutral button press
                dialog.cancel()
            }
            //.setNegativeButton(getString(R.string.decline)) { dialog, which ->
            // Respond to negative button press
            //}
            .setPositiveButton(getString(R.string.yes_continue)) { _, _ ->
                openURL.data = Uri.parse("https://jacktor.com/user/" + data.username + "/")
                startActivity(openURL)
            }
            .show()

    }

    //Visit Profile on Author button
    private fun visitProfileAuthor() {
        val openURL = Intent(Intent.ACTION_VIEW)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.visit_this_profile_title))
            .setMessage(getString(R.string.author_name) + " jacktor")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                // Respond to neutral button press
                dialog.cancel()
            }
            //.setNegativeButton(getString(R.string.decline)) { dialog, which ->
            // Respond to negative button press
            //}
            .setPositiveButton(getString(R.string.yes_continue)) { _, _ ->
                openURL.data = Uri.parse("https://jacktor.com/user/jacktor/")
                startActivity(openURL)
            }
            .show()
    }


    //changelog
    private fun changelog() {
        val inputStream = requireContext().resources.openRawResource(R.raw.changelog)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val customView = layoutInflater.inflate(R.layout.changelog_dialog, null)
        val progressBar = customView.findViewById<LinearProgressIndicator>(R.id.progressBar)

        var i: Int
        try {
            i = inputStream.read()
            while (i != -1) {
                byteArrayOutputStream.write(i)
                i = inputStream.read()
            }
            inputStream.close()
        } catch (e: IOException) {
            //return null
        }

        customView.findViewById<TextView>(R.id.changelog_txt).text = byteArrayOutputStream.toString()
        progressBar?.visibility = View.GONE

        dialog.setView(customView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { dialogBtn, _ ->
                // Respond to neutral button press
                dialogBtn.cancel()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}