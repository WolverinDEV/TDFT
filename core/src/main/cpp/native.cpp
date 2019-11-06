#include "./native.h"
#include <string>
#include <cassert>
#include <experimental/filesystem>

namespace fs = std::experimental::filesystem;

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     dev_wolveringer_tdft_Native
 * Method:    cd
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_dev_wolveringer_tdft_Native_cd(JNIEnv *env, jclass, jstring jtarget) {
	auto target_length = env->GetStringUTFLength(jtarget);
	auto target_data = env->GetStringUTFChars(jtarget, nullptr);

	std::string target;
	target.assign(target_data, target_length);

	env->ReleaseStringUTFChars(jtarget, target_data);

	std::error_code ec{};
	fs::current_path(fs::u8path(target), ec);
	if(ec) {
		auto ex_class = env->FindClass("java/lang/Exception");
		assert(ex_class);

		auto error = std::to_string(ec.value()) + "/" + ec.message();
		env->ThrowNew(ex_class, error.c_str());
	}
}

#ifdef __cplusplus
}
#endif