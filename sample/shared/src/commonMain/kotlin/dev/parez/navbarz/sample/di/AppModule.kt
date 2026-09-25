package dev.parez.navbarz.sample.di

import dev.parez.navbarz.sample.PokemonApi
import dev.parez.navbarz.sample.PokemonRepository
import dev.parez.navbarz.sample.SettingsState
import dev.parez.navbarz.sample.TeamState
import dev.parez.navbarz.sample.ui.PokemonDetailViewModel
import dev.parez.navbarz.sample.ui.PokemonListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { PokemonApi() }
    single { PokemonRepository(get()) }
    single { TeamState() }
    single { SettingsState() }
    viewModelOf(::PokemonListViewModel)
    viewModel { params -> PokemonDetailViewModel(params.get(), get()) }
}
