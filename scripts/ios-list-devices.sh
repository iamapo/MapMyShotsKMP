#!/bin/sh
set -eu

exec xcrun devicectl list devices
