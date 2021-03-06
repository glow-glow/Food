package com.example.food.ui.fragments.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food.viewmodels.MainViewModel
import com.example.food.R
import com.example.food.adapter.RecipesAdapter
import com.example.food.databinding.FragmentRecipesBinding
import com.example.food.util.Constants.Companion.API_KEY
import com.example.food.util.NetworkResult
import com.example.food.util.observeOnce
import com.example.food.viewmodels.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_recipes.view.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var recipesViewModel: RecipesViewModel
    private val mAdapter by lazy { RecipesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        recipesViewModel = ViewModelProvider(requireActivity()).get(RecipesViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mianViewModel = mainViewModel



         setupRecyclerView()
         readDatabase()
         binding.recipesFab.setOnClickListener{
             findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
         }

        return binding.root
    }

    private fun setupRecyclerView(){
        binding.recyclerview?.adapter = mAdapter
        binding.recyclerview?.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()

    }

    private fun readDatabase() {
       lifecycleScope.launch {
           mainViewModel.readRecipe.observeOnce(viewLifecycleOwner,{ database ->
               if(database.isNotEmpty()){
                   Log.d("RecipesFragment","readeDatabase called")

                   mAdapter.setDate(database[0].foodRecipes)
                   hideShimmerEffect()

               }else{
                   requestApiData()
               }

           })
       }

    }


    private fun requestApiData(){
        Log.d("RecipesFragment","requestApiData called")
        mainViewModel.getRecipes(recipesViewModel.applyQueries())
        mainViewModel.recipesResponse.observe(viewLifecycleOwner,{ response->
            when(response){
                is NetworkResult.Success->{
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setDate(it) }
                }
                is NetworkResult.Error->{
                    hideShimmerEffect()
                    loadDataFromCache()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading->{
                    showShimmerEffect()
                }
            }

        } )

    }

    private fun loadDataFromCache(){
      lifecycleScope.launch {
          mainViewModel.readRecipe.observe(viewLifecycleOwner,{ database->
              if(database.isNotEmpty()){
                  mAdapter.setDate(database[0].foodRecipes)
              }

          })
      }

    }





    private fun showShimmerEffect(){
        binding.recyclerview?.showShimmer()

    }

    private fun hideShimmerEffect(){
        binding.recyclerview?.hideShimmer()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}