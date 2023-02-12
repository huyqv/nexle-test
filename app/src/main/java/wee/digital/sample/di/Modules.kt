package wee.digital.sample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import wee.digital.sample.data.ApiService
import wee.digital.sample.ui.fragment.auth.AuthVM

val viewModelModule = module {
    viewModel { AuthVM(get()) }
}

val apiServiceModule = module {
    single(createdAtStart = false) {
        get<Retrofit>().create(ApiService::class.java)
    }
}


val allModules = module {
    includes(
        listOf(
            viewModelModule,
            retrofitModule,
            apiServiceModule
        )
    )
}