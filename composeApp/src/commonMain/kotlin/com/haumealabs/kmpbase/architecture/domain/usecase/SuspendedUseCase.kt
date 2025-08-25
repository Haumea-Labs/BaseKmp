package com.haumealabs.kmpbase.architecture.domain.usecase

import com.haumealabs.kmpbase.architecture.domain.result.UseCaseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * SuspendedUseCase provides a common pattern for asynchronous background executed use cases
 *
 * @param P the type for parameters
 * @param R the type for results
 *
 * @author <a href="mailto:info@movilok.com">Movilok Interactividad Movil S.L.</a>
 */
abstract class SuspendedUseCase<in P, R> : CoroutineScope {

  /**
   * The coroutine context
   */
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main

  /**
   * The use case execution in background
   *
   * @param params
   * @param onResult
   */
  operator fun invoke(params: P, onResult: (UseCaseResult<R>) -> Unit = {}) {
    launch(coroutineContext) {
      val result = withContext(Dispatchers.IO) { run(params) }
      onResult(result)
    }
  }

  /**
   * The use case task to be executed
   *
   * @param params the parameters for the use case
   * @return the result from the use case
   */
  abstract suspend fun run(params: P): UseCaseResult<R>

}
