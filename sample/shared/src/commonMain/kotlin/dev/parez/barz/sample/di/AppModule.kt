package dev.parez.barz.sample.di

import dev.parez.barz.sample.PokemonApi
import dev.parez.barz.sample.PokemonRepository
import dev.parez.barz.sample.SettingsState
import dev.parez.barz.sample.TeamState
import dev.parez.barz.sample.ui.PokemonDetailViewModel
import dev.parez.barz.sample.ui.PokemonListViewModel
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
