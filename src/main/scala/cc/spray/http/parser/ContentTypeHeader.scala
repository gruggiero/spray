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

package cc.spray.http
package parser

import org.parboiled.scala._
import HttpStatusCodes._
import HttpHeaders._

private[parser] trait ContentTypeHeader {
  this: Parser with ProtocolParameterRules with CommonActions =>

  def CONTENT_TYPE = rule (
    MediaTypeDecl ~ EOI
  )
  
  def MediaTypeDecl = rule (
    MediaTypeDef ~~> (createContentTypeHeader(_, _, _))
  )
  
  private def createContentTypeHeader(mainType: String, subType: String, params: Map[String, String]) = {
    val mimeType = getMediaType(mainType, subType)
    params.get("charset").map { charsetName =>
      Charsets.getForKey(charsetName.toLowerCase).getOrElse {
        throw new HttpException(BadRequest, "Unsupported charset: " + charsetName)
      }
    } match {
      case Some(charset) => `Content-Type`(ContentType(mimeType, charset)) 
      case None => `Content-Type`(ContentType(mimeType)) 
    }
  } 
  
}