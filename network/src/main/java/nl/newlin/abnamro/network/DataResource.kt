package nl.newlin.abnamro.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed class DataResource<out T> where T : Any? {

    data object Loading : DataResource<Nothing>()

    class Success<T>(val data: T) : DataResource<T>()
    class Error(val exception: Throwable?) : DataResource<Nothing>()
}

internal fun <T> fetchResource(apiCall: suspend () -> T): Flow<DataResource<T>> = flow {
    emit(DataResource.Loading)
    emit(DataResource.Success(apiCall()))
}.catch {
    emit(DataResource.Error(it))
}