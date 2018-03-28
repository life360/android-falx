#!/usr/bin/env bash
# Test curl command to trigger a Ci build using a local config.yml file
#
# https://discuss.circleci.com/t/run-builds-on-circleci-using-a-local-config-file/17355
#
# Token: Generate one (if needed) from here: https://circleci.com/account/api
# Use: export CIRCLE_TOKEN={YOUR_GENERATED_TOKEN} before running
# revision = a git commit hash from the remote branch (e.g. latest commit in 'master' remote branch
# Url: https://circleci.com/api/v1.1/project/github/life360/android-falx/tree/master
#                                           /vcs   /org    /project          /branch
#
curl --user ${CIRCLE_TOKEN}: \
  --request POST \
  --form revision=0e2cc8bc3b722373226d04761b62e1dfd786ad03 \
  --form config=@config.yml \
  --form notify=false \
    https://circleci.com/api/v1.1/project/github/life360/android-falx/tree/master
