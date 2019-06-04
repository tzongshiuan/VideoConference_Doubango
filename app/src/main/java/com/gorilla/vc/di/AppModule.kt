package com.gorilla.vc.di

import android.app.Application
import android.content.Context
import com.gorilla.vc.api.ApiService
import com.gorilla.vc.api.HostSelectionInterceptor
import com.gorilla.vc.model.PreferencesHelper
import com.gorilla.vc.model.VcPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.doubango.ngn.NgnEngine
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApiService(preferencesHelper : PreferencesHelper): ApiService {
        val ipSelectInterceptor = object : HostSelectionInterceptor(preferencesHelper.ip){
            override var host:String = preferencesHelper.ip
                get() = preferencesHelper.ip
        }

        val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client : OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(ipSelectInterceptor)
            this.addInterceptor(interceptor)
        }.readTimeout(5, TimeUnit.SECONDS)
         .connectTimeout(5, TimeUnit.SECONDS)
         .build()

        try {
            return Retrofit.Builder()
                    .baseUrl(preferencesHelper.ip)
//                    .baseUrl("http://192.168.10.224:13667/")
//                .baseUrl("http://192.168.10.204:8081/")
//                .baseUrl("http://192.168.10.228:8050/")
                    .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)   //show log
                    .build()
                    .create(ApiService::class.java)
        }catch (e: Exception){
            return Retrofit.Builder()
                    .baseUrl("http://192.168.10.29")// and error server ip
//                    .baseUrl(preferencesHelper.getServerIp())
//                .baseUrl("http://192.168.10.204:8081/")
//                .baseUrl("http://192.168.10.228:8050/")
                    .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)   //show log
                    .build()
                    .create(ApiService::class.java)
        }
    }

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(vcPreferences: VcPreferences): PreferencesHelper {
        return vcPreferences
    }

    @Provides
    @Singleton
    fun provideNgnEngine(): NgnEngine {
        return NgnEngine.getInstance()
    }

    @Provides
    @Singleton
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }
}