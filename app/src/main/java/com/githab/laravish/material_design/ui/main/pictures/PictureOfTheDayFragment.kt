package com.githab.laravish.material_design.ui.main.pictures


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.RoundedCornersTransformation
import com.githab.laravish.material_design.*
import com.githab.laravish.material_design.databinding.FragmentPictureBinding
import com.githab.laravish.material_design.ui.main.AppState
import com.githab.laravish.material_design.ui.main.PictureOfTheDayViewModel


class PictureOfTheDayFragment : Fragment() {

    private var _binding: FragmentPictureBinding? = null
    private val binding: FragmentPictureBinding
        get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this)[PictureOfTheDayViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLiveData().observe(viewLifecycleOwner) { appState ->
            renderData(appState)
        }
        viewModel.sentRequest(TODAY)
        searchWiki()
        chipGroupOnClick()
    }

    private fun chipGroupOnClick() = with(binding) {
        chipToday.setOnClickListener {
            viewModel.sentRequest(TODAY)
        }
        chipYesterday.setOnClickListener {
            viewModel.sentRequest(YESTERDAY)
        }
        chipToDaysAgo.setOnClickListener {
            viewModel.sentRequest(TO_DAYS_AGO)
        }
    }

    private fun searchWiki() {
        binding.inputLayout.setEndIconOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data =
                    Uri.parse("$URL_WIKI${binding.editText.text.toString()}")
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun renderData(appState: AppState) = with(binding) {
        when (appState) {
            is AppState.Error -> {
                Toast.makeText(requireContext(),
                    getString(R.string.error_loading),
                    Toast.LENGTH_LONG).show()
            }
            AppState.Loading -> {
                Toast.makeText(requireContext(), getString(R.string.loading), Toast.LENGTH_LONG)
                    .show()
            }
            is AppState.Success -> {
                imageView.load(appState.pictureOfTheDayResponseData.url) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    transformations(RoundedCornersTransformation(50f))
                    error(R.drawable.ic_launcher_background)
                }
                setText(appState)
            }
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setText(appState: AppState.Success) = with(binding) {

        textView.text = appState.pictureOfTheDayResponseData.explanation
        textView.typeface =
            Typeface.createFromAsset(requireActivity().assets, "font/cd2f1-36d91_sunday.ttf")


        val spannableString: SpannableString

        val text =
            "My text \nbullet one \nbulleterter two\nbullet wetwwefrtweteone \nbullet wetwettwo\nbullet wetwetwone \nbullet two"
        val result = text.indexesOf("\n")
        spannableString = SpannableString(text)
        Log.d("myLog", "$result")
        var current = result.first()
        result.forEach {
            if (current != it) {
                spannableString.setSpan(BulletSpan(20,
                    ContextCompat.getColor(requireContext(), R.color.red),
                    20),
                    current + 1, it, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            current = it
        }
        spannableString.setSpan(BulletSpan(20,
            ContextCompat.getColor(requireContext(), R.color.red),
            20),
            current + 1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        for (i in text.indices) {
            if (text[i] == 't') {
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(),
                    R.color.red)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        val bitmap = ContextCompat.getDrawable(requireContext(), R.drawable.ic_mars)
        val verticalAlignment = DynamicDrawableSpan.ALIGN_CENTER
        val widthInPx = 50
        val heightInPx = 50
        bitmap?.setBounds(0, 0, widthInPx, heightInPx)
        for (i in text.indices) {
            if (text[i] == 'o') {
                spannableString.setSpan(bitmap?.let { ImageSpan(it, verticalAlignment) },
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = spannableString
        }

    }

    fun String.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> =
        (if (ignoreCase) Regex(substr, RegexOption.IGNORE_CASE) else Regex(substr))
            .findAll(this).map { it.range.first }.toList()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance() = PictureOfTheDayFragment()
    }
}

