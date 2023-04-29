package com.jacktorscript.batterynotifier.ui.about

class DataModel {

    var name: String? = null
    var username: String? = null
    var projectStatus: String? = null
    var imgURL: String? = null

    fun getNames(): String {
        return name.toString()
    }

    fun setNames(name: String) {
        this.name = name
    }

    fun getUsernames(): String {
        return username.toString()
    }

    fun setUsernames(username: String) {
        this.username = username
    }

    fun getStatus(): String {
        return projectStatus.toString()
    }

    fun setStatus(projectStatus: String) {
        this.projectStatus = projectStatus
    }

    fun getimgURLs(): String {
        return imgURL.toString()
    }

    fun setimgURLs(imgURL: String) {
        this.imgURL = imgURL
    }

}