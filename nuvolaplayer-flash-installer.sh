#!/bin/bash
#
# About
# =====
#
# This tool wraps 32bit Adobe Flash plugin and installs it to a special
# directory for Nuvola Player > 2.0.0 (both 32bit and 64bit).
#
# Rationale
# =========
#
# Adobe Flash plugin is incompatible with Nuvola Player 2.0.x, because it is
# not possible to mix GTK+ 2 (Flash plugin) and GTK+ 3 (Nuvola Player) in the
# same process.
#
# Long term solution of this incompatibility is to port Nuvola Player
# to WebKit2Gtk API, that runs plugins in a separate process. Unfortunately,
# the new API is not yet available in any stable Linux distribution, so it is
# necessary to use a temporary workaround for now.
#
# Short term workaround is to wrap Flash plugin via nspluginwrapper.
# Unfortunately, this way has many drawbacks:
# 
#  * The primary goal of nspluginwrapper was to wrap 32bit Flash plugin for
#    64bit systems. Since native 64bit Flash plugin is available, this goal
#    is no longer valid and nspluginwrapper is considered deprecated. Moreover,
#    some Linux distributions (e.g. Debian) have already removed nspluginwrapper
#    from repositories.
#
#  * nspluginwrapper can wrap only 32bit plugins, so you cannot use native 64bit
#    plugin on 64bit system, you have to use wrapped 32bit plugin.
#
#  * Wrapped Flash plugin has higher CPU usage and tends to crash more often.
#
#  * Since nspluginwrapper is dead, there may be unresolved security issues.
#
# To minimize negative effects on your system, this tool doesn't install
# wrapped Flash plugin into standard directory for web plugins
# (/usr/lib/mozilla/plugins), but to a directory specific for Nuvola Player
# (/opt/nuvolaplayer/flash), so your web browsers can use native Flash plugin,
# whereas Nuvola Player will use wrapped Flash plugin.
#
# Note: You need Nuvola Player 2.0.1 and higher to use the wrapped plugin from
# the special directory, Nuvola Player 2.0.0 won't find it.
#
# Copyright
# =========
#
# Copyright 2012 Jiří Janoušek <janousek.jiri@gmail.com>
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met: 
# 
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer. 
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution. 
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

CMD="$(basename "$0")"
VERSION="0.1.0"
USAGE="Wrapped Flash Plugin installer for Nuvola Player

Usage:

    $CMD --install=VERSION [--force] [--debug]
    $CMD --help|--version

Options:

    --install=VERSION    Install given version of Flash plugin.
    --force              Force overwrite destination directory.
    --debug              Script debugging.
    --help               Prints usage.
    --version            Prints version."
NSPLUGINWRAPPER_VERSION="1.4.4"
FLASHPLUGIN=""
FLASHDIR="/opt/nuvolaplayer/flash"
ORIG_FLASHDIR="$FLASHDIR/orig"
WRAPPED_FLASHDIR="$FLASHDIR/wrapped"
FORCE=0

# helper for fatal error messages
fatal(){
    echo "Error: $*" >&2
    exit 1
}

[ "$#" = "0" ] && fatal "Not enough arguments.

$USAGE"

# Parse options

for option in "$@"; do
    case $option in
        --help)
            echo "$USAGE"
            exit 0
            ;;
        --version)
            echo "Version: $VERSION"
            exit 0
            ;;
        --force)
            FORCE=1
            ;;
        --debug)
            set -x
            ;;
        --install=*)
            FLASH_VERSION="$(echo "$option" | cut -f 2 -d "=")"
            FLASHPLUGIN="http://archive.canonical.com/pool/partner/a/adobe-flashplugin/adobe-flashplugin_${FLASH_VERSION}.orig.tar.gz"
            ;;
       *)
            fatal "Unknown argument '$option'.

$USAGE"
            ;;
    esac
done

[ -z "$FLASHPLUGIN" ] && fatal fatal "Not enough arguments.

$USAGE"

# Wanna root rights
if [ "$(id -ru)" != "0" ]; then
  fatal "You have to run this tool as root (superuser)."
fi

# Check dependencies.

NSPLUGINWRAPPER="$(which nspluginwrapper 2>/dev/null)"
#~ [ -z "$NSPLUGINWRAPPER" ] && fatal "Program 'nspluginwrapper' has not been found."

WGET="$(which wget 2>/dev/null)"
[ -z "$WGET" ] && fatal "Program 'wget' has not been found."

#~ AWK="$(which awk 2>/dev/null)"
#~ [ -z "$AWK" ] && fatal "Program 'awk' has not been found."

TAR="$(which tar 2>/dev/null)"
[ -z "$TAR" ] && fatal "Program 'tar' has not been found."

GZIP="$(which gzip 2>/dev/null)"
[ -z "$GZIP" ] && fatal "Program 'gzip' has not been found."


# Warn user if untested version of nspluginwrapper is used.
[ "$($NSPLUGINWRAPPER | grep Version | cut -f 6 -d " ")" != "$NSPLUGINWRAPPER_VERSION" ] \
&& echo "WARNING: This tool has been tested only with nspluginwrapper $NSPLUGINWRAPPER_VERSION." >&2

set -e

# Make sure $FLASHDIR doesn't exist.
if [ -d "$FLASHDIR" ]; then
    [ "$FORCE" != "1" ] && fatal "Directory '$FLASHDIR' already exists. Use '$CMD --force' to overwrite it."
    rm -rfv "$FLASHDIR"
fi

# Create necessary directories
mkdir -pv "$ORIG_FLASHDIR" "$WRAPPED_FLASHDIR"
cd "$ORIG_FLASHDIR"

echo
# Get 32bit Adobe Flash plugin
$WGET "$FLASHPLUGIN"
echo
$TAR -xvzf "$(basename "$FLASHPLUGIN")"
echo
mv -v adobe-flashplugin*/i386/libflashplayer.so .
echo
rm -rfv adobe-flashplugin*

echo
# Wrap the plugin and move it to directory specific for Nuvola Player
WRAPPING="$(nspluginwrapper -v -n -i "$ORIG_FLASHDIR/libflashplayer.so")"
echo $WRAPPING
echo
#~ WRAPPED_FLASH="$(echo "$WRAPPING" | $AWK '/^\s+into\s/ { print $2 }')"
WRAPPED_FLASH="/usr/lib/mozilla/plugins/npwrapper.libflashplayer.so"
mv -v "$WRAPPED_FLASH" "$WRAPPED_FLASHDIR/npwrapper.libflashplayer.so"

echo "
Wrapped Flash plugin has been succesfully installed to "$WRAPPED_FLASHDIR/npwrapper.libflashplayer.so".

You have to restart Nuvola Player in order to use this plugin.

Note: You need Nuvola Player 2.0.1 or higher to use the wrapped plugin from
the special directory, Nuvola Player 2.0.0 won't find it."""
