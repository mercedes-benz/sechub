// SPDX-License-Identifier: MIT

package util

import (
	"testing"

	. "mercedes-benz.com/sechub/testutil"
)

func Test_Simple_filename_and_pattern_with_asterisk_at_start_and_prefix_matches(t *testing.T) {
	AssertTrue(FilePathMatch("a.txt", "*.txt"), t)
}

func Test_Simple_filename_and_pattern_with_asterisk_but_more_matches(t *testing.T) {
	/* must match*/
	AssertTrue(FilePathMatch("a1.txt", "a*1.txt"), t)
	AssertTrue(FilePathMatch("a_bla_bla_1.txt", "a*1.txt"), t)
	AssertTrue(FilePathMatch("a1.txt", "a*.*"), t)
	AssertTrue(FilePathMatch("a1.doc", "a*.*"), t)
	AssertTrue(FilePathMatch("a.doc", "a*.*"), t)

	/* must fail*/
	AssertFalse(FilePathMatch("a2.txt", "a1.txt"), t)
	AssertFalse(FilePathMatch("a2.txt", "a*1.txt"), t)
	AssertFalse(FilePathMatch("a2.txt", "a1*.txt"), t)
}

func TestXYZ_When_NO_double_asterisk_and_path_is_same_it_matches(t *testing.T) {
	/* exact match*/
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/gargamel/schlumpfine/testfolder/a.txt"), t)

	/* no match, no wildcards */
	AssertFalse(FilePathMatch("/x/y/z/V/a.txt", "a.txt"), t)
}

func Test_When_double_asterisk_on_start_any_path_is_accepted_when_filename_without_asterisk_matches(t *testing.T) {
	/* filenames all matching but different pathes */
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "**/a.txt"), t)
	AssertTrue(FilePathMatch("/home/gargamel/testfolder/a.txt", "**/a.txt"), t)
	AssertTrue(FilePathMatch("/x/y/z/V/a.txt", "**/a.txt"), t)
	AssertTrue(FilePathMatch("a.txt", "**/a.txt"), t)

	/* but not when filename does not match*/
	AssertFalse(FilePathMatch("/x/y/z/V/b.txt", "**/a.txt"), t)
	AssertFalse(FilePathMatch("/x/y/z/V/a.txt", "**/b.txt"), t)
}

func Test_When_double_asterisk_on_start_and_filename_with_asterisk(t *testing.T) {
	AssertFalse(FilePathMatch("/x/y/z/V/a.txtx", "**/*.txt"), t)
	AssertTrue(FilePathMatch("/x/y/z/V/b.txt", "**/*.txt"), t)
}

func Test_When_double_asterisk_also_matching_files_in_current_working_directory(t *testing.T) {
	AssertTrue(FilePathMatch("/.git/x/y/z/V/a.txt", "**/.git/**"), t)
	AssertTrue(FilePathMatch("/.git/a.txt", "**/.git/**"), t)
	AssertTrue(FilePathMatch(".git/x/y/z/V/a.txt", "**/.git/**"), t)
	AssertTrue(FilePathMatch(".git/a.txt", "**/.git/**"), t)
}

func Test_When_double_asterisk_on_inside_path_is_accepted_when_filename_without_asterisk_matches(t *testing.T) {
	/* filenames all matching but different pathes */
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/**/a.txt"), t)
	AssertTrue(FilePathMatch("/home/gargamel/testfolder/a.txt", "/home/gargamel/**/a.txt"), t)
	AssertTrue(FilePathMatch("/x/y/z/V/a.txt", "/x/y/z/**/a.txt"), t)

	/* but not when filename does not match*/
	AssertFalse(FilePathMatch("/x/y/z/V/b.txt", "/x/**/a.txt"), t)
	AssertFalse(FilePathMatch("/x/y/z/V/a.txt", "/x/**/b.txt"), t)
}

func Test_When_multiple_double_asterisk_on_inside_path_is_accepted_when_filename_without_asterisk_matches(t *testing.T) {
	/* filenames all matching but different pathes */
	AssertTrue(FilePathMatch("/x/y/z/V/a.txt", "/x/**/V/a.txt"), t)
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/**/schlumpfine/**/a.txt"), t)
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/**/**/a.txt"), t)
	AssertTrue(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/**/a.txt"), t)

	/* but not when filename does not match*/
	AssertFalse(FilePathMatch("/x/y/z/V/a.txt", "/x/**/V/b.txt"), t)
	AssertFalse(FilePathMatch("/home/gargamel/schlumpfine/testfolder/a.txt", "/home/**/schlaubi/**/a.txt"), t)
}

//func Test_Path_and_simple_filename_and_pattern_with_asterisk_at_start_and_prefix_matches(t *testing.T) {
//	AssertTrue(FilePathMatch("/home/albert/a.txt","/home/albert/*.txt"), t)
//}
