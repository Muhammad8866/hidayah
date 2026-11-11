# Deen Companion Naat catalog

`catalog.json` lists the five voice-only tracks shown in Deen Companion. The app keeps a built-in catalog as a fallback if GitHub is unavailable.

The audio is currently streamed from the Internet Archive item `nasheednomusic`. A listener can download each track within the app for offline playback. This repository does not redistribute those recordings because the Archive item does not specify a redistribution license.

If permission to mirror a recording is obtained, upload it to a release and add its `githubUrl` to that track's catalog entry. The app will then stream and download from the GitHub release URL. Keep the track ID unchanged so existing offline downloads continue to work.
