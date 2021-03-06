/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray
package builders

import http._
import HttpMethods._
import util.matching.Regex

private[spray] trait SimpleFilterBuilders {
  this: FilterBuilders =>
  
  /**
   * Returns a Route filter that rejects all non-DELETE requests.
   */
  def delete  = method(DELETE)

  /**
   * Returns a Route filter that rejects all non-GET requests.
   */
  def get     = method(GET)

  /**
   * Returns a Route filter that rejects all non-HEAD requests.
   */
  def head    = method(HEAD)

  /**
   * Returns a Route filter that rejects all non-OPTIONS requests.
   */
  def options = method(OPTIONS)

  /**
   * Returns a Route filter that rejects all non-POST requests.
   */
  def post    = method(POST)

  /**
   * Returns a Route filter that rejects all non-PUT requests.
   */
  def put     = method(PUT)

  /**
   * Returns a Route filter that rejects all non-TRACE requests.
   */
  def trace   = method(TRACE)

  /**
   * Returns a Route filter that rejects all requests whose HTTP method does not match the given one.
   */
  def method(m: HttpMethod): FilterRoute0 = filter { ctx =>
    if (ctx.request.method == m) Pass() else Reject(MethodRejection(m)) 
  }

  /**
   * Returns a Route filter that rejects all requests with a host name different from the given one.
   */
  def host(hostName: String): FilterRoute0 = host(_ == hostName)

  /**
   * Returns a Route filter that rejects all requests for whose host name the given predicate function return false.
   */
  def host(predicate: String => Boolean): FilterRoute0 = filter { ctx =>
    if (predicate(ctx.request.host)) Pass() else Reject()
  }

  /**
   * Returns a Route filter that rejects all requests with a host name that does not have a prefix matching the given
   * regular expression. For all matching requests the prefix string matching the regex is extracted and passed to
   * the inner Route building function. If the regex contains a capturing group only the string matched by this group
   * is extracted. If the regex contains more than one capturing group an IllegalArgumentException will be thrown.
   */
  def host(regex: Regex): FilterRoute1[String] = filter1 { ctx =>
    def run(regexMatch: String => Option[String]) = {
      regexMatch(ctx.request.host) match {
        case Some(matched) => Pass(matched)
        case None => Reject()
      }
    }
    regex.groupCount match {
      case 0 => run(regex.findPrefixOf(_))
      case 1 => run(regex.findPrefixMatchOf(_).map(_.group(1)))
      case 2 => throw new IllegalArgumentException("Path regex '" + regex.pattern.pattern +
              "' must not contain more than one capturing group")
    }
  }
  
}
