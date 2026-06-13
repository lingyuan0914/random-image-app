package com.randomimage.di

import com.randomimage.data.remote.KoriImageApi
import com.randomimage.data.remote.KoriService
import com.randomimage.data.remote.LoliconImageApi
import com.randomimage.data.remote.LoliconService
import com.randomimage.data.remote.MoeImgImageApi
import com.randomimage.data.remote.MwmImageApi
import com.randomimage.data.remote.SexPhotoImageApi
import com.randomimage.data.remote.SexPhotoService
import com.randomimage.data.remote.XjhImageApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("lolicon")
    fun provideLoliconRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.lolicon.app/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideLoliconService(@Named("lolicon") retrofit: Retrofit): LoliconService {
        return retrofit.create(LoliconService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoliconApi(service: LoliconService): LoliconImageApi {
        return LoliconImageApi(service)
    }

    @Provides
    @Singleton
    fun provideMoeImgApi(): MoeImgImageApi {
        return MoeImgImageApi()
    }

    @Provides
    @Singleton
    @Named("sexphoto")
    fun provideSexPhotoRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://sex.nyan.run/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideSexPhotoService(@Named("sexphoto") retrofit: Retrofit): SexPhotoService {
        return retrofit.create(SexPhotoService::class.java)
    }

    @Provides
    @Singleton
    fun provideSexPhotoApi(service: SexPhotoService): SexPhotoImageApi {
        return SexPhotoImageApi(service)
    }

    @Provides
    @Singleton
    @Named("kori")
    fun provideKoriRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api.kori.moe/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideKoriService(@Named("kori") retrofit: Retrofit): KoriService {
        return retrofit.create(KoriService::class.java)
    }

    @Provides
    @Singleton
    fun provideKoriApi(service: KoriService): KoriImageApi {
        return KoriImageApi(service)
    }

    @Provides
    @Singleton
    fun provideXjhApi(): XjhImageApi {
        return XjhImageApi()
    }

    @Provides
    @Singleton
    fun provideMwmApi(): MwmImageApi {
        return MwmImageApi()
    }
}
