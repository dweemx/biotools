"""

    PHYLIP wrapper for python
    
    author: Matt Rasmussen
    date:   2/4/2007


The following programs should be in your PATH
    seqboot     -- Sequence bootstrapping

"""


# python imports
import os
import sys

def execPhylip(cmd, args, verbose=False):
    """Execute a phylip-like program that expects arguments from stdin"""

    if verbose:
        print("exec: %s" % cmd)
        print("args: %s" % args)
        assert os.system("""cat <<EOF | %s %s""" % (cmd, args)) == 0
    else:
        assert os.system("""cat <<EOF | %s >/dev/null 2>&1 %s""" % (cmd, args)) == 0
seed = 617
verbose = True
# Run python script with argument : http://stackoverflow.com/questions/14155669/call-python-script-from-bash-with-argument
# First argument is the file name itself
file_path = sys.argv[1]
iters = int(sys.argv[2])
print(file_path)
execPhylip("seqboot", "\n%s\nr\n%d\ny\n%d" % (file_path, iters, seed), verbose)