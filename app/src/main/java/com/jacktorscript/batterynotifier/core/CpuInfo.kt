package com.jacktorscript.batterynotifier.core

import android.os.Build
import java.io.File
import java.io.FileFilter
import java.io.RandomAccessFile
import java.util.regex.Pattern


object CpuInfo {

    /*
     * return current cpu usage (0 to 100) guessed from core frequencies
     */

    fun getCpuUsageFromFreq(): Int {
        return getCpuUsage(getCoresUsageGuessFromFreq)
    }

    /*
     * @return total cpu usage (from 0 to 100) since last call of getCpuUsage or getCoresUsage
     *         first call always returns 0 as previous value is not known
     * ! deprecated since oreo !
     */
    fun getCpuUsageSinceLastCall(): Int {
        return if (Build.VERSION.SDK_INT < 26) getCpuUsage(getCoresUsageDeprecated) else 0
    }


    private fun getCpuUsage(coresUsage: IntArray): Int {
        // compute total cpu usage from each core as the total cpu usage given by /proc/stat seems
        // not considering offline cores: i.e. 2 cores, 1 is offline, total cpu usage given by /proc/stat
        // is equal to remaining online core (should be remaining online core / 2).
        var cpuUsage = 0
        if (coresUsage.size < 2) return 0
        for (i in 1 until coresUsage.size) {
            if (coresUsage[i] > 0) cpuUsage += coresUsage[i]
        }
        return cpuUsage / (coresUsage.size - 1)
    }

    /*
    * guess core usage using core frequency (e.g. all core at min freq => 0% usage;
    *   all core at max freq => 100%)
    *
    * This function is compatible with android oreo and later but is less precise than
    *   getCoresUsageDeprecated.
    * This function returns the current cpu usage (not the average usage since last call).
    *
    * @return array of cores usage
    *   array size = nbcore +1 as the first element is for global cpu usage
    *   array element: 0 => cpu at 0% ; 100 => cpu at 100%
    */
    @get:Synchronized
    val getCoresUsageGuessFromFreq: IntArray
        get() {
            initCoresFreq()
            val nbCores = mCoresFreq!!.size + 1
            val coresUsage = IntArray(nbCores)
            coresUsage[0] = 0
            for (i in mCoresFreq!!.indices) {
                coresUsage[i + 1] = mCoresFreq!![i].curUsage
                coresUsage[0] += coresUsage[i + 1]
            }
            if (mCoresFreq!!.size > 0) coresUsage[0] /= mCoresFreq!!.size
            return coresUsage
        }

    private fun initCoresFreq() {
        if (mCoresFreq == null) {
            val nbCores = nbCores
            mCoresFreq = ArrayList()
            for (i in 0 until nbCores) {
                mCoresFreq!!.add(CoreFreq(i))
            }
        }
    }

    private fun getCurCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq")
    }

    private fun getMinCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_min_freq")
    }

    private fun getMaxCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq")
    }

    // return 0 if any pb occurs
    private fun readIntegerFile(path: String): Int {
        var ret = 0
        try {
            val reader = RandomAccessFile(path, "r")
            ret = reader.use {
                val line = it.readLine()
                line.toInt()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ret
    }
    // from https://stackoverflow.com/questions/7962155/how-can-you-detect-a-dual-core-cpu-on-an-android-device-from-code//Default to return 1 core//Get directory containing CPU info
    //Filter to only list the devices we care about
    //Return the number of cores (virtual CPU devices)
//Check if filename is "cpu", followed by one or more digits//Private Class to display only CPU devices in the directory listing
    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private val nbCores: Int
        get() {
            //Private Class to display only CPU devices in the directory listing
            class CpuFilter : FileFilter {
                override fun accept(pathname: File): Boolean {
                    //Check if filename is "cpu", followed by one or more digits
                    return Pattern.matches("cpu[0-9]+", pathname.name)
                }
            }
            return try {
                //Get directory containing CPU info
                val dir = File("/sys/devices/system/cpu/")
                //Filter to only list the devices we care about
                val files = dir.listFiles(CpuFilter())
                //Return the number of cores (virtual CPU devices)
                files!!.size
            } catch (e: Exception) {
                //Default to return 1 core
                1
            }
        }

    // current cores frequencies
    private var mCoresFreq: ArrayList<CoreFreq>? = null// compute usage
    // check strange values

    // cur becomes prev (only if cpu online)

    // load another line only if corresponding core has been found
    // otherwise try next core number with same line
// check for strange values// cpu lines are only at the top of the file

    // try get core stat number i
/* cat /proc/stat # example of possible output
             *   cpu  193159 118453 118575 7567474 4615 6 2312 0 0 0
             *   cpu0 92389 116352 96662 2125638 2292 5 2021 0 0 0
             *   cpu3 47648 1264 11220 2378965 1286 0 9 0 0 0
             *   ...
             */// +1 for global cpu stat

    // ensure mPrevCores list is big enough
    //new CpuStat(-1, -1));

    // init cpuStats
    /** */ /* !!! deprecated since oreo !!! */ /*
     * @return array of cores usage since last call
     *   (first call always returns -1 as the func has never been called).
     *   array size = nbcore +1 as the first element is for global cpu usage
     *   First element is global CPU usage from stat file (which does not consider offline core !
     *     Use getCpuUsage do get proper global CPU usage)
     *   array element: < 0 => cpu unavailable ; 0 => cpu min ; 100 => cpu max
     */
    @get:Synchronized
    val getCoresUsageDeprecated: IntArray
        get() {
            val numCores = nbCores + 1 // +1 for global cpu stat

            // ensure mPrevCores list is big enough
            if (mPrevCoreStats == null) mPrevCoreStats = ArrayList()
            while (mPrevCoreStats!!.size < numCores) mPrevCoreStats!!.add(null) //new CpuStat(-1, -1));

            // init cpuStats
            val coreStats = ArrayList<CoreStat?>()
            while (coreStats.size < numCores) coreStats.add(null)
            val coresUsage = IntArray(numCores)
            for (i in 0 until numCores) coresUsage[i] = -1
            try {
                /* cat /proc/stat # example of possible output
                 *   cpu  193159 118453 118575 7567474 4615 6 2312 0 0 0
                 *   cpu0 92389 116352 96662 2125638 2292 5 2021 0 0 0
                 *   cpu3 47648 1264 11220 2378965 1286 0 9 0 0 0
                 *   ...
                 */
                val reader = RandomAccessFile("/proc/stat", "r")
                reader.use {
                    var curCoreStat: CoreStat?
                    var line = it.readLine()
                    for (i in 0 until numCores) {
                        // cpu lines are only at the top of the file
                        if (!line.contains("cpu")) break

                        // try get core stat number i
                        curCoreStat = readCoreStat(i, line)
                        if (curCoreStat != null) {
                            val prevCoreStat = mPrevCoreStats!![i]
                            if (prevCoreStat != null) {
                                val diffActive = curCoreStat.active - prevCoreStat.active
                                val diffTotal = curCoreStat.total - prevCoreStat.total
                                // check for strange values
                                if (diffActive > 0 && diffTotal > 0) // compute usage
                                    coresUsage[i] = (diffActive * 100 / diffTotal).toInt()
                                // check strange values
                                if (coresUsage[i] > 100) coresUsage[i] = 100
                                if (coresUsage[i] < 0) coresUsage[i] = 0
                            }

                            // cur becomes prev (only if cpu online)
                            mPrevCoreStats!![i] = curCoreStat

                            // load another line only if corresponding core has been found
                            // otherwise try next core number with same line
                            line = it.readLine()
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return coresUsage
        }

    /* return CpuStat read, or null if it could not be read (e.g. cpu offline)
    * @param coreNum coreNum=0 return global cpu state, coreNum=1 return first core
    *
    * adapted from https://stackoverflow.com/questions/22405403/android-cpu-cores-reported-in-proc-stat
    */
    private fun readCoreStat(coreNum: Int, line: String): CoreStat? {
        var coreStat: CoreStat? = null
        try {
            val cpuStr: String = if (coreNum > 0) "cpu" + (coreNum - 1) + " " else "cpu "
            if (line.contains(cpuStr)) {
                val toks = line.split(" +").toTypedArray()

                // we are recording the work being used by the user and
                // system(work) and the total info of cpu stuff (total)
                // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438
                // user  nice  system  idle  iowait  irq  softirq  steal
                val active = toks[1].toLong() + toks[2].toLong() + toks[3].toLong()
                val total =
                    toks[1].toLong() + toks[2].toLong() + toks[3].toLong() + toks[4].toLong() + toks[5].toLong() + toks[6].toLong() + toks[7].toLong() + toks[8].toLong()
                //                long active = total - Long.parseLong(toks[4]);
                coreStat = CoreStat(active.toFloat(), total.toFloat())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return coreStat
    }

    // previous stat read
    private var mPrevCoreStats: ArrayList<CoreStat?>? = null

    private class CoreFreq(var num: Int) {
        var cur = 0
        var min = 0
        var max = 0

        init {
            min = getMinCpuFreq(num)
            max = getMaxCpuFreq(num)
        }

        fun updateCurFreq() {
            cur = getCurCpuFreq(num)
            // min & max cpu could not have been properly initialized if core was offline
            if (min == 0) min = getMinCpuFreq(num)
            if (max == 0) max = getMaxCpuFreq(num)
        }//                if (cur == min)

        //                    cpuUsage = 2; // consider lowest freq as 2% usage (usually core is offline if 0%)
//                else
        /* return usage from 0 to 100 */
        val curUsage: Int
            get() {
                updateCurFreq()
                var cpuUsage = 0
                if (max - min > 0 && max > 0 && cur > 0) {
                    //                if (cur == min)
                    //                    cpuUsage = 2; // consider lowest freq as 2% usage (usually core is offline if 0%)
                    //                else
                    cpuUsage = (cur - min) * 100 / (max - min)
                }
                return cpuUsage
            }
    }

    private class CoreStat(var active: Float, var total: Float)
}