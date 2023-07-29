# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

from firebase_functions import https_fn
from firebase_admin import initialize_app, auth, firestore

initialize_app()
#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")


@https_fn.on_call(max_instances=1)
def deleteUser(req: https_fn.CallableRequest) -> any:
    uid = req.data["uid"]
    try:
        # Delete the user using Firebase Admin SDK
        auth.delete_user(uid)
    except Exception:
        pass
    except auth.AuthError:
        pass
        
    try:
        # Delete the user document in Firestore
        db = firestore.client()
        user_doc_ref = db.collection('Users').document(uid)
        user_doc_ref.delete()

        
    except firestore.NotFound:
        pass
        #return https_fn.Response('User document not found in Firestore.', status_code=500)
    except Exception as e:
        pass
        #return https_fn.Response(str(e), status_code=500)
    return https_fn.Response('success')


@https_fn.on_call(max_instances=1)
def updateUserName(req: https_fn.CallableRequest) -> any:
    uid = req.data["uid"]
    newName = req.data["name"]
    try:
        auth.update_user(
            uid,
            display_name=newName)
    except Exception:
        pass
    return https_fn.Response('success', status_code=200)