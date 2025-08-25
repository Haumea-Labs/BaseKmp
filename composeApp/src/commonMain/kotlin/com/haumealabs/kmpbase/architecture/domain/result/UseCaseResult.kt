package com.haumealabs.kmpbase.architecture.domain.result

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 *
 * @author <a href="mailto:info@movilok.com">Movilok Interactividad Movil S.L.</a>
 */
sealed class UseCaseResult<out R> {

  /**
   * Successful result execution
   *
   * @param T the result data type
   * @property data the result data content
   */
  data class Success<out T>(val data: T) : UseCaseResult<T>()

  /**
   * Failed result execution
   *
   * @property error the resulting error
   */
  data class Error(val error: Throwable) : UseCaseResult<Nothing>()

  /**
   * Provides a result data representation as text
   *
   * @return the text
   */
  override fun toString(): String {
    return when (this) {
      is Success<*> -> "Success[data=$data]"
      is Error -> "Error[error=$error]"
    }
  }

}

/**
 * Returns `true` if [Result] is of type [UseCaseResult.Success] & holds non-null [UseCaseResult.Success.data].
 */
val UseCaseResult<*>.succeeded
  get() = this is UseCaseResult.Success && data != null
