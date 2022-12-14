import base64
import html
import json
import os
import random
import re
import string
import time
import jwt
import urllib.parse
import requests
from traceable.ast.context import ScanContext, PluginContext
from traceable.ast.testsuite import AttributeList
from traceable.ast.testsuite.assertion import Assertion
from traceable.config import logger

def crapi_creds_prehook(scanctx: ScanContext, pluginctx: PluginContext, attributes: AttributeList) -> list[Assertion]:
    url = urllib.parse.urlparse(
        attributes.get_one("original.http.request.url", ""))
    logger.info("Running crapi_login_prehook for plugin %s" % pluginctx.get_plugin())
    if scanctx.get("crapi.role.user", None) is None:
        # Login:
        logger.debug("crapi_login_prehook: Token not found. Creating token")
        u = urllib.parse.urlunparse(
            url._replace(path="/identity/api/auth/login"))
        res = requests.post(u, json={
            "email": "test@example.com",
            "password": "Test!123"
        }, headers={
            "content-type": "application/json",
            "x-traceable-ast": "0,0,0"
        })
        if res.status_code != 200:
            logger.error("Failed to login to crapi, got status %d" %
                         res.status_code)
            return []

        res = res.json()
        scanctx.set("crapi.role.user", res["token"])
    else:
        logger.debug("Already logged in to CRAPI")
    attributes.set("mutated.auth.attribute", "mutated.http.request.header.authorization")
    attributes.set("mutated.role.user", "Bearer %s" % scanctx.get("crapi.role.user"))
    logger.debug("Mutated role user: %s" % scanctx.get("crapi.role.user"))
    return []

def crapi_login_posthook(scanctx: ScanContext, pluginctx: PluginContext, attributes: AttributeList) -> list[Assertion]:
    res_code = int(attributes.get_one("mutated.http.response.code", 0))
    # err_code = attributes.get_one("mutated.http.response.json.error", "")
    if res_code == 401 and scanctx["crapi_token"]:
        del scanctx["crapi_token"]
    return []


def crapi_login_prehook(scanctx: ScanContext, pluginctx: PluginContext, attributes: AttributeList) -> list[Assertion]:
    url = urllib.parse.urlparse(
        attributes.get_one("original.http.request.url", ""))
    logger.info("Attempting login to CRAPI")
    if scanctx.get("crapi_token", None) is None:
        # Login:
        u = urllib.parse.urlunparse(
            url._replace(path="/identity/api/auth/login"))
        res = requests.post(u, json={
            "email": scanctx.get("crapi_user", "test@example.com"),
            "password": "Traceable1!"
        }, headers={
            "content-type": "application/json"
        })
        if res.status_code != 200:
            logger.error("Failed to login to crapi, got status %d" %
                         res.status_code)
            return []

        res = res.json()
        scanctx["crapi_token"] = res["token"]
        logger.info("got crapi_token: %s" % scanctx["crapi_token"])
    else:
        logger.info("Already logged in to CRAPI")
    attributes.set("mutated.auth.attribute", "mutated.http.request.header.authorization")
    attributes.set("mutated.auth.role.user", "Bearer %s" % scanctx["crapi_token"])
    return []