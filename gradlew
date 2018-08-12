#!/bin/sh

(command -v docker >/dev/null 2>&1 && {
  docker run --rm -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:jdk8-alpine \
  gradle $@
}) || \
(command -v gradle >/dev/null 2>&1 && {
  gradle $@
}) || \
{
  echo >&2 "docker or gradle required but could not find either installed. Aborting..."
  exit 1
}
