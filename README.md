1.  Did you code stage() / how well does it work?
> I did! All it does is call Aviv's existing code, and I think my implementation is good.
2.  Did you code commit() / how well does it work?
> I did! A commit generates a good looking commit and tree file.
3. Did you do checkout / how well does it work?
> I did! when you stage everything and make a commit and check it out it recreates the working directory.
4. What bugs did find / which of em did you fix?
> The paths in trees and index were incorrect:
> Trees from new Blob were not added to index due to a recursion bug (although subtrees were)
> The absolute path of the current directory was hardcoded and relied on
> An absolute path was used in tree files in some scenarious
I think there were a couple others, and some I haven't seen, but overall I think this was super readable mega-awesome code.